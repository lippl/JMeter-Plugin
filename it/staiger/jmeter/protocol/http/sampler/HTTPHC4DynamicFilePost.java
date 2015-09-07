/*
 * @@@LICENSE
 *
 */

package it.staiger.jmeter.protocol.http.sampler;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import it.staiger.jmeter.protocol.http.sampler.DynamicHttpPostSampler;
import it.staiger.jmeter.protocol.http.util.VariableFileArg;
import it.staiger.jmeter.services.FileContentServer;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.DefaultedHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jmeter.protocol.http.sampler.HTTPHC4Impl;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.MeasuringConnectionManager;
import org.apache.jmeter.protocol.http.sampler.HttpClientDefaultParameters;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.util.HC4TrustAllSSLSocketFactory;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.SlowHC4SSLSocketFactory;
import org.apache.jmeter.protocol.http.util.SlowHC4SocketFactory;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JsseSSLManager;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * HTTP Sampler using Apache HttpClient 4.x.
 *
 */
public class HTTPHC4DynamicFilePost extends HTTPHC4Impl {

    private static final Logger log = LoggingManager.getLoggerForClass();

    /** retry count to be used (default 0); 0 = disable retries */
    private static final int RETRY_COUNT = JMeterUtils.getPropDefault("httpclient4.retrycount", 0);

    /** Idle timeout to be applied to connections if no Keep-Alive header is sent by the server (default 0 = disable) */
    private static final int IDLE_TIMEOUT = JMeterUtils.getPropDefault("httpclient4.idletimeout", 0);

    private static final String CONTEXT_METRICS = "jmeter_metrics"; // TODO hack for metrics related to HTTPCLIENT-1081, to be removed later

    protected final DynamicHttpPostSampler testElement;
    
    private static final ConnectionKeepAliveStrategy IDLE_STRATEGY = new DefaultConnectionKeepAliveStrategy(){
        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            long duration = super.getKeepAliveDuration(response, context);
            if (duration <= 0 && IDLE_TIMEOUT > 0) {// none found by the superclass
                log.debug("Setting keepalive to " + IDLE_TIMEOUT);
                return IDLE_TIMEOUT;
            }
            return duration; // return the super-class value
        }
        
    };

    /**
     * Special interceptor made to keep metrics when connection is released for some method like HEAD
     * Otherwise calling directly ((HttpConnection) localContext.getAttribute(ExecutionContext.HTTP_CONNECTION)).getMetrics();
     * would throw org.apache.http.impl.conn.ConnectionShutdownException
     * See https://bz.apache.org/jira/browse/HTTPCLIENT-1081
     */
    private static final HttpResponseInterceptor METRICS_SAVER = new HttpResponseInterceptor(){
        @Override
        public void process(HttpResponse response, HttpContext context)
                throws HttpException, IOException {
            HttpConnection conn = (HttpConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION);
            HttpConnectionMetrics metrics = conn.getMetrics();
            context.setAttribute(CONTEXT_METRICS, metrics);
        }
    };
    private static final HttpRequestInterceptor METRICS_RESETTER = new HttpRequestInterceptor() {
        @Override
        public void process(HttpRequest request, HttpContext context)
                throws HttpException, IOException {
            HttpConnection conn = (HttpConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION);
            HttpConnectionMetrics metrics = conn.getMetrics();
            metrics.reset();
        }
    };

    /**
     * 1 HttpClient instance per combination of (HttpClient,HttpClientKey)
     */
    private static final ThreadLocal<Map<HttpClientKey, HttpClient>> HTTPCLIENTS_CACHE_PER_THREAD_AND_HTTPCLIENTKEY = 
        new ThreadLocal<Map<HttpClientKey, HttpClient>>(){
        @Override
        protected Map<HttpClientKey, HttpClient> initialValue() {
            return new HashMap<HttpClientKey, HttpClient>();
        }
    };

    // Scheme used for slow HTTP sockets. Cannot be set as a default, because must be set on an HttpClient instance.
    private static final Scheme SLOW_HTTP;
    
    // We always want to override the HTTPS scheme, because we want to trust all certificates and hosts
    private static final Scheme HTTPS_SCHEME;

    /*
     * Create a set of default parameters from the ones initially created.
     * This allows the defaults to be overridden if necessary from the properties file.
     */
    private static final HttpParams DEFAULT_HTTP_PARAMS;

    static {
        log.info("HTTP request retry count = "+RETRY_COUNT);
        
        DEFAULT_HTTP_PARAMS = new SyncBasicHttpParams(); // Could we drop the Sync here?
        DEFAULT_HTTP_PARAMS.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
        DefaultHttpClient.setDefaultHttpParams(DEFAULT_HTTP_PARAMS);
        
        // Process Apache HttpClient parameters file
        String file=JMeterUtils.getProperty("hc.parameters.file"); // $NON-NLS-1$
        if (file != null) {
            HttpClientDefaultParameters.load(file, DEFAULT_HTTP_PARAMS);
        }

        // Set up HTTP scheme override if necessary
        if (CPS_HTTP > 0) {
            log.info("Setting up HTTP SlowProtocol, cps="+CPS_HTTP);
            SLOW_HTTP = new Scheme(HTTPConstants.PROTOCOL_HTTP, HTTPConstants.DEFAULT_HTTP_PORT, new SlowHC4SocketFactory(CPS_HTTP));
        } else {
            SLOW_HTTP = null;
        }
        
        // We always want to override the HTTPS scheme
        Scheme https = null;
        if (CPS_HTTPS > 0) {
            log.info("Setting up HTTPS SlowProtocol, cps="+CPS_HTTPS);
            try {
                https = new Scheme(HTTPConstants.PROTOCOL_HTTPS, HTTPConstants.DEFAULT_HTTPS_PORT, new SlowHC4SSLSocketFactory(CPS_HTTPS));
            } catch (GeneralSecurityException e) {
                log.warn("Failed to initialise SLOW_HTTPS scheme, cps="+CPS_HTTPS, e);
            }
        } else {
            log.info("Setting up HTTPS TrustAll scheme");
            try {
                https = new Scheme(HTTPConstants.PROTOCOL_HTTPS, HTTPConstants.DEFAULT_HTTPS_PORT, new HC4TrustAllSSLSocketFactory());
            } catch (GeneralSecurityException e) {
                log.warn("Failed to initialise HTTPS TrustAll scheme", e);
            }
        }
        HTTPS_SCHEME = https;
        if (localAddress != null){
            DEFAULT_HTTP_PARAMS.setParameter(ConnRoutePNames.LOCAL_ADDRESS, localAddress);
        }
        
    }

    private volatile HttpUriRequest currentRequest; // Accessed from multiple threads

    private volatile boolean resetSSLContext;
/*
    protected HTTPHC4wPropAsFileImpl(HTTPSamplerBase testElement) {
        super(testElement);
    }
*/
    protected HTTPHC4DynamicFilePost(DynamicHttpPostSampler testElement) {
        super((HTTPSamplerBase) testElement);
    	this.testElement=testElement;
    }
    
    @Override
    protected HTTPSampleResult sample(URL url, String method,
            boolean areFollowingRedirect, int frameDepth) {

        if (log.isDebugEnabled()) {
            log.debug("Start : sample " + url.toString());
            log.debug("method " + method+ " followingRedirect " + areFollowingRedirect + " depth " + frameDepth);            
        }

        HTTPSampleResult res = createSampleResult(url, method);

        HttpClient httpClient = setupClient(url, res);

        HttpRequestBase httpRequest = null;
        try {
            URI uri = url.toURI();
            if (method.equals(HTTPConstants.POST)) {
                httpRequest = new HttpPost(uri);
            } else {
                throw new IllegalArgumentException("Unexpected method: '"+method+"'");
            }
            setupRequest(url, httpRequest, res); // can throw IOException
        } catch (Exception e) {
            res.sampleStart();
            res.sampleEnd();
            errorResult(e, res);
            return res;
        }

        HttpContext localContext = new BasicHttpContext();
        
        res.sampleStart();

        final CacheManager cacheManager = getCacheManager();
        if (cacheManager != null && HTTPConstants.GET.equalsIgnoreCase(method)) {
           if (cacheManager.inCache(url)) {
               return updateSampleResultForResourceInCache(res);
           }
        }

        try {
            currentRequest = httpRequest;

            // attach POST data
            String postBody = sendPostData((HttpPost)httpRequest);
            res.setQueryString(postBody);
            
            // perform the sample
            HttpResponse httpResponse = 
                    executeRequest(httpClient, httpRequest, localContext, url);

            // parse response and fill the SampleResult with all info
            parseResponse(httpRequest, localContext, res, httpResponse);

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(httpResponse, res.getURL(), getCookieManager());

            // Save cache information
            if (cacheManager != null){
                cacheManager.saveDetails(httpResponse, res);
            }

            // Follow redirects and download page resources if appropriate:
            res = resultProcessing(areFollowingRedirect, frameDepth, res);

        } catch (IOException e) {
            log.debug("IOException", e);
            if (res.getEndTime() == 0) {
                res.sampleEnd();
            }
           // pick up headers if failed to execute the request
            if (res.getRequestHeaders() != null) {
                log.debug("Overwriting request old headers: " + res.getRequestHeaders());
            }
            res.setRequestHeaders(getConnectionHeaders((HttpRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST)));
            errorResult(e, res);
            return res;
        } catch (RuntimeException e) {
            log.debug("RuntimeException", e);
            if (res.getEndTime() == 0) {
                res.sampleEnd();
            }
            errorResult(e, res);
            return res;
        } finally {
            currentRequest = null;
        }
        return res;
    }

    /**
     * Execute request either as is or under PrivilegedAction 
     * if a Subject is available for url
     * @param httpClient
     * @param httpRequest
     * @param localContext
     * @param url
     * @return
     * @throws IOException
     * @throws ClientProtocolException
     */
    private HttpResponse executeRequest(final HttpClient httpClient,
            final HttpRequestBase httpRequest, final HttpContext localContext, final URL url)
            throws IOException, ClientProtocolException {
        AuthManager authManager = getAuthManager();
        if (authManager != null) {
            Subject subject = authManager.getSubjectForUrl(url);
            if(subject != null) {
                try {
                    return Subject.doAs(subject,
                            new PrivilegedExceptionAction<HttpResponse>() {
    
                                @Override
                                public HttpResponse run() throws Exception {
                                    return httpClient.execute(httpRequest,
                                            localContext);
                                }
                            });
                } catch (PrivilegedActionException e) {
                    log.error(
                            "Can't execute httpRequest with subject:"+subject,
                            e);
                    throw new RuntimeException("Can't execute httpRequest with subject:"+subject, e);
                }
            }
        }
        return httpClient.execute(httpRequest, localContext);
    }

    /**
     * Holder class for all fields that define an HttpClient instance;
     * used as the key to the ThreadLocal map of HttpClient instances.
     */
    private static final class HttpClientKey {

        private final String target; // protocol://[user:pass@]host:[port]
        private final boolean hasProxy;
        private final String proxyHost;
        private final int proxyPort;
        private final String proxyUser;
        private final String proxyPass;
        
        private final int hashCode; // Always create hash because we will always need it

        /**
         * @param url URL Only protocol and url authority are used (protocol://[user:pass@]host:[port])
         * @param hasProxy has proxy
         * @param proxyHost proxy host
         * @param proxyPort proxy port
         * @param proxyUser proxy user
         * @param proxyPass proxy password
         */
        public HttpClientKey(URL url, boolean hasProxy, String proxyHost,
                int proxyPort, String proxyUser, String proxyPass) {
            // N.B. need to separate protocol from authority otherwise http://server would match https://erver (<= sic, not typo error)
            // could use separate fields, but simpler to combine them
            this.target = url.getProtocol()+"://"+url.getAuthority();
            this.hasProxy = hasProxy;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.proxyUser = proxyUser;
            this.proxyPass = proxyPass;
            this.hashCode = getHash();
        }
        
        private int getHash() {
            int hash = 17;
            hash = hash*31 + (hasProxy ? 1 : 0);
            if (hasProxy) {
                hash = hash*31 + getHash(proxyHost);
                hash = hash*31 + proxyPort;
                hash = hash*31 + getHash(proxyUser);
                hash = hash*31 + getHash(proxyPass);
            }
            hash = hash*31 + target.hashCode();
            return hash;
        }

        // Allow for null strings
        private int getHash(String s) {
            return s == null ? 0 : s.hashCode(); 
        }
        
        @Override
        public boolean equals (Object obj){
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof HttpClientKey)) {
                return false;
            }
            HttpClientKey other = (HttpClientKey) obj;
            if (this.hasProxy) { // otherwise proxy String fields may be null
                return 
                this.hasProxy == other.hasProxy &&
                this.proxyPort == other.proxyPort &&
                this.proxyHost.equals(other.proxyHost) &&
                this.proxyUser.equals(other.proxyUser) &&
                this.proxyPass.equals(other.proxyPass) &&
                this.target.equals(other.target);
            }
            // No proxy, so don't check proxy fields
            return 
                this.hasProxy == other.hasProxy &&
                this.target.equals(other.target);
        }

        @Override
        public int hashCode(){
            return hashCode;
        }

        // For debugging
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(target);
            if (hasProxy) {
                sb.append(" via ");
                sb.append(proxyUser);
                sb.append("@");
                sb.append(proxyHost);
                sb.append(":");
                sb.append(proxyPort);
            }
            return sb.toString();
        }
    }

    private HttpClient setupClient(URL url, SampleResult res) {

        Map<HttpClientKey, HttpClient> mapHttpClientPerHttpClientKey = HTTPCLIENTS_CACHE_PER_THREAD_AND_HTTPCLIENTKEY.get();
        
        final String host = url.getHost();
        final String proxyHost = getProxyHost();
        final int proxyPort = getProxyPortInt();

        boolean useStaticProxy = isStaticProxy(host);
        boolean useDynamicProxy = isDynamicProxy(proxyHost, proxyPort);

        // Lookup key - must agree with all the values used to create the HttpClient.
        HttpClientKey key = new HttpClientKey(url, (useStaticProxy || useDynamicProxy), 
                useDynamicProxy ? proxyHost : PROXY_HOST,
                useDynamicProxy ? proxyPort : PROXY_PORT,
                useDynamicProxy ? getProxyUser() : PROXY_USER,
                useDynamicProxy ? getProxyPass() : PROXY_PASS);
        
        HttpClient httpClient = mapHttpClientPerHttpClientKey.get(key);

        if (httpClient != null && resetSSLContext && HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(url.getProtocol())) {
            ((AbstractHttpClient) httpClient).clearRequestInterceptors(); 
            ((AbstractHttpClient) httpClient).clearResponseInterceptors(); 
            httpClient.getConnectionManager().closeIdleConnections(1L, TimeUnit.MICROSECONDS);
            httpClient = null;
            JsseSSLManager sslMgr = (JsseSSLManager) SSLManager.getInstance();
            sslMgr.resetContext();
            resetSSLContext = false;
        }

        if (httpClient == null){ // One-time init for this client

            HttpParams clientParams = new DefaultedHttpParams(new BasicHttpParams(), DEFAULT_HTTP_PARAMS);

            DnsResolver resolver = this.testElement.getDNSResolver();
            if (resolver == null) {
                resolver = new SystemDefaultDnsResolver();
            }
            ClientConnectionManager connManager = new MeasuringConnectionManager(SchemeRegistryFactory.createDefault(), resolver);

            httpClient = new DefaultHttpClient(connManager, clientParams) {
                @Override
                protected HttpRequestRetryHandler createHttpRequestRetryHandler() {
                    return new DefaultHttpRequestRetryHandler(RETRY_COUNT, false); // set retry count
                }
            };
            if (IDLE_TIMEOUT > 0) {
                ((AbstractHttpClient) httpClient).setKeepAliveStrategy(IDLE_STRATEGY );
            }
            ((AbstractHttpClient) httpClient).addResponseInterceptor(new ResponseContentEncoding());
            ((AbstractHttpClient) httpClient).addResponseInterceptor(METRICS_SAVER); // HACK
            ((AbstractHttpClient) httpClient).addRequestInterceptor(METRICS_RESETTER); 
            
            // Override the defualt schemes as necessary
            SchemeRegistry schemeRegistry = httpClient.getConnectionManager().getSchemeRegistry();

            if (SLOW_HTTP != null){
                schemeRegistry.register(SLOW_HTTP);
            }

            if (HTTPS_SCHEME != null){
                schemeRegistry.register(HTTPS_SCHEME);
            }

            // Set up proxy details
            if (useDynamicProxy){
                HttpHost proxy = new HttpHost(proxyHost, proxyPort);
                clientParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                String proxyUser = getProxyUser();
                
                if (proxyUser.length() > 0) {                   
                    ((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(
                            new AuthScope(proxyHost, proxyPort),
                            new NTCredentials(proxyUser, getProxyPass(), localHost, PROXY_DOMAIN));
                }
            } else if (useStaticProxy) {
                HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT);
                clientParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                if (PROXY_USER.length() > 0) {
                    ((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(
                            new AuthScope(PROXY_HOST, PROXY_PORT),
                            new NTCredentials(PROXY_USER, PROXY_PASS, localHost, PROXY_DOMAIN));
                }
            }

            // Bug 52126 - we do our own cookie handling
            clientParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);

            if (log.isDebugEnabled()) {
                log.debug("Created new HttpClient: @"+System.identityHashCode(httpClient) + " " + key.toString());
            }

            mapHttpClientPerHttpClientKey.put(key, httpClient); // save the agent for next time round
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Reusing the HttpClient: @"+System.identityHashCode(httpClient) + " " + key.toString());
            }
        }

        MeasuringConnectionManager connectionManager = (MeasuringConnectionManager) httpClient.getConnectionManager();
        connectionManager.setSample(res);

        // TODO - should this be done when the client is created?
        // If so, then the details need to be added as part of HttpClientKey
        setConnectionAuthorization(httpClient, url, getAuthManager(), key);

        return httpClient;
    }


    /**
     * Gets the ResponseHeaders
     *
     * @param response
     *            containing the headers
     * @return string containing the headers, one per line
     */
    private String getResponseHeaders(HttpResponse response) {
        StringBuilder headerBuf = new StringBuilder();
        Header[] rh = response.getAllHeaders();
        headerBuf.append(response.getStatusLine());// header[0] is not the status line...
        headerBuf.append("\n"); // $NON-NLS-1$

        for (int i = 0; i < rh.length; i++) {
            headerBuf.append(rh[i].getName());
            headerBuf.append(": "); // $NON-NLS-1$
            headerBuf.append(rh[i].getValue());
            headerBuf.append("\n"); // $NON-NLS-1$
        }
        return headerBuf.toString();
    }

    /**
     * Get all the request headers for the <code>HttpMethod</code>
     *
     * @param method
     *            <code>HttpMethod</code> which represents the request
     * @return the headers as a string
     */
    private String getConnectionHeaders(HttpRequest method) {
        if(method != null) {
            // Get all the request headers
            StringBuilder hdrs = new StringBuilder(100);
            Header[] requestHeaders = method.getAllHeaders();
            for(int i = 0; i < requestHeaders.length; i++) {
                // Exclude the COOKIE header, since cookie is reported separately in the sample
                if(!HTTPConstants.HEADER_COOKIE.equalsIgnoreCase(requestHeaders[i].getName())) {
                    hdrs.append(requestHeaders[i].getName());
                    hdrs.append(": "); // $NON-NLS-1$
                    hdrs.append(requestHeaders[i].getValue());
                    hdrs.append("\n"); // $NON-NLS-1$
                }
            }
    
            return hdrs.toString();
        }
        return ""; ////$NON-NLS-1$
    }

    /**
     * Setup credentials for url AuthScope but keeps Proxy AuthScope credentials
     * @param client HttpClient
     * @param url URL
     * @param authManager {@link AuthManager}
     * @param key key
     */
    private void setConnectionAuthorization(HttpClient client, URL url, AuthManager authManager, HttpClientKey key) {
        CredentialsProvider credentialsProvider = 
            ((AbstractHttpClient) client).getCredentialsProvider();
        if (authManager != null) {
            if(authManager.hasAuthForURL(url)) {
                authManager.setupCredentials(client, url, credentialsProvider, localHost);
            } else {
                credentialsProvider.clear();
            }
        } else {
            Credentials credentials = null;
            AuthScope authScope = null;
            if(key.hasProxy && !StringUtils.isEmpty(key.proxyUser)) {
                authScope = new AuthScope(key.proxyHost, key.proxyPort);
                credentials = credentialsProvider.getCredentials(authScope);
            }
            credentialsProvider.clear(); 
            if(credentials != null) {
                credentialsProvider.setCredentials(authScope, credentials);
            }
        }
    }


    // Helper class so we can generate request data without dumping entire file contents to SampleResult
    private static class ViewableByteBody extends ByteArrayBody {
        private boolean hideFileData;
        
        public ViewableByteBody(byte[] content, String mimeType, String fileName) {
            super(content, mimeType, fileName);
            hideFileData = false;
        }

        @Override
        public void writeTo(final OutputStream out) throws IOException {
            if (hideFileData) {
                out.write("<actual file content, not shown here>".getBytes());// encoding does not really matter here
            } else {
                super.writeTo(out);
            }
        }
    }
    
    /**
     * 
     * @param post {@link HttpPost}
     * @return String posted body if computable
     * @throws IOException if sending the data fails due to I/O
     */
    @Override
    protected String sendPostData(HttpPost post)  throws IOException {
        // Buffer to hold the post body, except file content
        StringBuilder postedBody = new StringBuilder(1000);
        FileContentServer contentServer = FileContentServer.getServer();
        HTTPFileArg staticFiles[] = getHTTPFiles();
        HTTPFileArg dynFiles[] = testElement.getDynamicFiles();
        VariableFileArg variableFiles[] = testElement.getVariableFiles();
    	String[] attachmentsNumber = null;
        boolean thresholdCheck = testElement.getRecordType()>=testElement.getThreshold();

        final String contentEncoding = getContentEncodingOrNull();
        final boolean haveContentEncoding = contentEncoding != null;
        boolean hasContent = false;

        
        // If a content encoding is specified, we use that as the
        // encoding of any parameter values
        Charset charset = null;
        if(haveContentEncoding) {
            charset = Charset.forName(contentEncoding);
        }

        // Write the request to our own stream
        MultipartEntity multiPart = new MultipartEntity(
                getDoBrowserCompatibleMultipart() ? HttpMultipartMode.BROWSER_COMPATIBLE : HttpMultipartMode.STRICT,
                        null, charset);
        // Create the parts
        // Add any parameters
        PropertyIterator args = getArguments().iterator();
        while (args.hasNext()) {
           HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
           String parameterName = arg.getName();
           if (arg.isSkippable(parameterName)){
               continue;
           }
           FormBodyPart formPart;
           StringBody stringBody = new StringBody(arg.getValue(), charset);
           formPart = new FormBodyPart(arg.getName(), stringBody);                   
           multiPart.addPart(formPart);
           hasContent = true;
        }
        /*
         * set parameters controlled by threshold
         */
        if(!testElement.getArgumentThreshold() || thresholdCheck){
	        args = testElement.getOwnArguments().iterator();
	        while (args.hasNext()) {
	           HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
	           String parameterName = arg.getName();
	           if (arg.isSkippable(parameterName)){
	               continue;
	           }
	           FormBodyPart formPart;
	           StringBody stringBody = new StringBody(arg.getValue(), charset);
	           formPart = new FormBodyPart(arg.getName(), stringBody);                   
	           multiPart.addPart(formPart);
	           hasContent = true;
	        }
        }

        // Add all files
        // Cannot retrieve parts once added to the MultiPartEntity, so have to save them here.
        ViewableByteBody[] viewableByteBodies = new ViewableByteBody[variableFiles.length+staticFiles.length+dynFiles.length];

        int i = 0;
        //Static Files
        if(!testElement.getStaticThreshold() || thresholdCheck)
	        for (i=0; i < staticFiles.length; i++) { 
	        	HTTPFileArg file = staticFiles[i];
	            
	            viewableByteBodies[i] = new ViewableByteBody(contentServer.getFileContent(file.getPath()), file.getMimeType(), new File(file.getPath()).getName());
	            multiPart.addPart(file.getParamName(),viewableByteBodies[i]);
	            hasContent = true;
	        }
        
        //Dynamic Files
        if(!testElement.getDynamicThreshold() || thresholdCheck){

    		attachmentsNumber = testElement.getAttachmentNumbers().split(",");
	        
	        for (int j=0; j < attachmentsNumber.length && !attachmentsNumber[j].isEmpty(); i++, j++) {
	        	int fileNum = Integer.parseInt(attachmentsNumber[j])-1;
	        	if(fileNum >= dynFiles.length){
	        		log.warn("trying to send file out of dynamic files range (" + Integer.toString(fileNum+1) + " of " + Integer.toString(dynFiles.length) + ")\nfile was skipped");
	        		i--;
	        		continue;
	        	}
	        	HTTPFileArg file = dynFiles[fileNum];

	            viewableByteBodies[i] = new ViewableByteBody(contentServer.getFileContent(file.getPath()), file.getMimeType(), new File(file.getPath()).getName());
	            multiPart.addPart(file.getParamName(),viewableByteBodies[i]);
	            hasContent = true;
	        }
        }
        
        //Variable Files
        if(!testElement.getVariableThreshold() || thresholdCheck)
	        for(int j = 0; j < variableFiles.length; j++, i++) {
	        	VariableFileArg file = variableFiles[j];
	            
	            viewableByteBodies[i] = new ViewableByteBody(file.getContent().getBytes(), file.getMimeType(), file.getName());
	            multiPart.addPart(file.getParamName(),viewableByteBodies[i]);
	            hasContent = true;
	        }
        
        post.setEntity(multiPart);
        if(!hasContent)
        	log.warn("POST has no content!");

        if (multiPart.isRepeatable()){
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            
            //stop the content from appearing in sampler result
            if(!testElement.getLogFiles()){
		        for(ViewableByteBody fileBody : viewableByteBodies){
		        	if(fileBody!=null)fileBody.hideFileData = true;
		        	else break;
		        }
            }
            
            multiPart.writeTo(bos);
            
            //Set it back, in order for the content to be sent
            if(!testElement.getLogFiles()){
		        for(ViewableByteBody fileBody : viewableByteBodies){
		        	if(fileBody!=null)fileBody.hideFileData = false;
		        	else break;
		        }
            }
            bos.flush();
            // We get the posted bytes using the encoding used to create it
            postedBody.append(new String(bos.toByteArray(),
                    contentEncoding == null ? "US-ASCII" // $NON-NLS-1$ this is the default used by HttpClient
                    : contentEncoding));
            bos.close();
        } else {
            postedBody.append("<Multipart was not repeatable, cannot view what was sent>"); // $NON-NLS-1$
        }
        
        return	postedBody.toString();
    }
    
    /**
     * Parses the result and fills the SampleResult with its info
     * @param httpRequest the executed request
     * @param localContext the Http context which was used
     * @param res the SampleResult which is to be filled
     * @param httpResponse the response which is to be parsed
     * @throws IllegalStateException
     * @throws IOException
     */
    private void parseResponse(HttpRequestBase httpRequest, HttpContext localContext, HTTPSampleResult res, HttpResponse httpResponse) throws IllegalStateException, IOException{
    	
    	// Needs to be done after execute to pick up all the headers
        final HttpRequest request = (HttpRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST);
        // We've finished with the request, so we can add the LocalAddress to it for display
        final InetAddress localAddr = (InetAddress) httpRequest.getParams().getParameter(ConnRoutePNames.LOCAL_ADDRESS);
        if (localAddr != null) {
            request.addHeader(HEADER_LOCAL_ADDRESS, localAddr.toString());
        }
        res.setRequestHeaders(getConnectionHeaders(request));

        Header contentType = httpResponse.getLastHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        if (contentType != null){
            String ct = contentType.getValue();
            res.setContentType(ct);
            res.setEncodingAndType(ct);                    
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            InputStream instream = entity.getContent();
            res.setResponseData(readResponse(res, instream, (int) entity.getContentLength()));
        }
        
        res.sampleEnd(); // Done with the sampling proper.
        currentRequest = null;

        // Now collect the results into the HTTPSampleResult:
        StatusLine statusLine = httpResponse.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        res.setResponseCode(Integer.toString(statusCode));
        res.setResponseMessage(statusLine.getReasonPhrase());
        res.setSuccessful(isSuccessCode(statusCode));

        res.setResponseHeaders(getResponseHeaders(httpResponse));
        if (res.isRedirect()) {
            final Header headerLocation = httpResponse.getLastHeader(HTTPConstants.HEADER_LOCATION);
            if (headerLocation == null) { // HTTP protocol violation, but avoids NPE
                throw new IllegalArgumentException("Missing location header in redirect for " + httpRequest.getRequestLine());
            }
            String redirectLocation = headerLocation.getValue();
            res.setRedirectLocation(redirectLocation);
        }

        // record some sizes to allow HTTPSampleResult.getBytes() with different options
        HttpConnectionMetrics  metrics = (HttpConnectionMetrics) localContext.getAttribute(CONTEXT_METRICS);
        long headerBytes = 
            res.getResponseHeaders().length()   // condensed length (without \r)
          + httpResponse.getAllHeaders().length // Add \r for each header
          + 1 // Add \r for initial header
          + 2; // final \r\n before data
        long totalBytes = metrics.getReceivedBytesCount();
        res.setHeadersSize((int) headerBytes);
        res.setBodySize((int)(totalBytes - headerBytes));
        if (log.isDebugEnabled()) {
            log.debug("ResponseHeadersSize=" + res.getHeadersSize() + " Content-Length=" + res.getBodySize()
                    + " Total=" + (res.getHeadersSize() + res.getBodySize()));
        }

        // If we redirected automatically, the URL may have changed
        if (getAutoRedirects()){
            HttpUriRequest req = (HttpUriRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST);
            HttpHost target = (HttpHost) localContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            URI redirectURI = req.getURI();
            if (redirectURI.isAbsolute()){
                res.setURL(redirectURI.toURL());
            } else {
                res.setURL(new URL(new URL(target.toURI()),redirectURI.toString()));
            }
        }
    }
    
    

    /**
     * 
     * @return the value of {@link #getContentEncoding()}; forced to null if empty
     */
    private String getContentEncodingOrNull() {
        return getContentEncoding(null);
    }

    /**
     * @param dflt the default to be used
     * @return the value of {@link #getContentEncoding()}; default if null or empty
     */
    private String getContentEncoding(String dflt) {
        String ce = getContentEncoding();
        if (isNullOrEmptyTrimmed(ce)) {
            return dflt;
        } else {
            return ce;
        }
    }

    private void saveConnectionCookies(HttpResponse method, URL u, CookieManager cookieManager) {
        if (cookieManager != null) {
            Header[] hdrs = method.getHeaders(HTTPConstants.HEADER_SET_COOKIE);
            for (Header hdr : hdrs) {
                cookieManager.addCookieFromHeader(hdr.getValue(),u);
            }
        }
    }

    @Override
    public boolean interrupt() {
        HttpUriRequest request = currentRequest;
        if (request != null) {
            currentRequest = null; // don't try twice
            try {
                request.abort();
            } catch (UnsupportedOperationException e) {
                log.warn("Could not abort pending request", e);
            }
        }
        return request != null;
    }
}
