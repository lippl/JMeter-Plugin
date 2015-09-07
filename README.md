# JMeter-Plugin
JMerter Plugin for optimized Load Testing. Implements a file content server, which extends the JMeter FileServer by making the content available in memory, instead of reading them every time from the disk. Also a HTTP Dynamic Post Sampler, which allows you to send files from JMeter Variables without having to create a File to upload. Also defineing a threshold value to switch endpoints and add files to the upload depending on it. Als it has a parameter to only upload a subset of the defined files. These can be chosen from an external config element or directly from the samplers files.
