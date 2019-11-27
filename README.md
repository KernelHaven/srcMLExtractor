# srcMLExtractor

![Build Status](https://jenkins-2.sse.uni-hildesheim.de/buildStatus/icon?job=KH_SrcMlExtractor)

A code-model extractor for [KernelHaven](https://github.com/KernelHaven/KernelHaven).

This extractor uses [srcML](https://www.srcml.org/) to analyze C source code.

## Capabilities

This extractors parsers C source code files (`*.c`, `*.h`) into an abstract syntax tree containing variability information.

## Usage

Place [`SrcMLExtractor.jar`](https://jenkins-2.sse.uni-hildesheim.de/job/KH_SrcMlExtractor/lastSuccessfulBuild/artifact/build/jar/SrcMLExtractor.jar) in the plugins folder of KernelHaven.

To use this extractor, set `code.extractor.class` to `net.ssehub.kernel_haven.srcml.SrcMLExtractor` in the KernelHaven properties.

## Dependencies

In addition to KernelHaven, this plugin has the following dependencies:
* [CppUtils](https://github.com/KernelHaven/CppUtils)
* Only runs on these platforms:
	* Windows (Vista, 7, 8, 10) 64 Bit
	* Linux x86 64 Bit; requires the following libraries (packages for Ubuntu 16.04): `libxml2, libxslt1.1, libarchive13, libssl1.0.0, libcurl3`
	* macOS: El Capitan (not tested)
	* Any platform, where srcML is installed (i.e. the binary `srcml` is available on the `PATH`)

## License

This plugin is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html).

Another license would be possible with the following restriction:
* The plugin contains [srcML](https://www.srcml.org/) which is under GPLv3. We do not link against srcML, so technically we are not infected by GPL. However a release under a license other than GPLv3 would require the removal of the contained srcML.

## Used Tools

The following tools are used (and bundled in `res/`) by this plugin:

| Tool | Version | License |
|------|---------|---------|
| [srcML](https://www.srcml.org/) | [Beta v0.9.5](https://www.srcml.org/#download) | [GPLv3](https://www.gnu.org/licenses/gpl.html) |
