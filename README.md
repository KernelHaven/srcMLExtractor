# srcMLExtractor

![Build Status](http://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=KernelHaven_SrcMlExtractor)

A code-model extractor for [KernelHaven](https://github.com/KernelHaven/KernelHaven).

This extractor uses [srcML](http://srcml.org/) to analyze C source code.

## Capabilities

This extractors parsers C source code files (`*.c`, `*.h`) into an abstract syntax tree containing variability information.

## Usage

Place [`SrcMLExtractor.jar`](https://jenkins.sse.uni-hildesheim.de/view/KernelHaven/job/KernelHaven_SrcMlExtractor/lastSuccessfulBuild/artifact/build/jar/SrcMLExtractor.jar) in the plugins folder of KernelHaven.

To use this extractor, set `code.extractor.class` to `net.ssehub.kernel_haven.srcml.SrcMLExtractor` in the KernelHaven properties.

## Dependencies

In addition to KernelHaven, this plugin has the following dependencies:
* Only runs on these platforms:
	* Windows (Vista, 7, 8, 10) 64 Bit
	* Linux x86 64 Bit; requires the following libraries (packages for Ubuntu 16.04): `libxml2, libxslt1.1, libarchive13, libssl1.0.0, libcurl3`
	* macOS: El Capitan (not tested)

## License

This plugin is licensed under GPLv3. Another license would be possible with following restrictions:

The plugin contains [srcML](http://srcml.org/) which is under GPL-3.0. We do not link against [srcML](http://srcml.org/), so technically we are not infected by GPL. However a release under a license other than GPL-3.0 would require the removal of the contained [srcML](http://srcml.org/).
