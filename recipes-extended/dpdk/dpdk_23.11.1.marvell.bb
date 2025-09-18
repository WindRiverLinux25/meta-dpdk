include dpdk.inc

SRC_URI = "git://github.com/MarvellEmbeddedProcessors/marvell-dpdk-sdk.git;protocol=https;branch=dpdk-23.11-release"
SRC_URI += " file://0001-config-meson-get-cpu_instruction_set-from-meson-opti.patch \
             file://CVE-2024-11614.patch \
             file://0001-net-ionic-fix-build-with-Fedora.patch \
             file://0001-net-gve-base-fix-build-with-Fedora.patch \
             file://0001-Add-new-tracepoint-function-for-type-time_t.patch \
             file://0001-meson.build-march-and-mcpu-already-passed-by-Yocto.patch \
             file://0001-drivers-net-octeontx-fix-build-issue.patch \
             file://0001-marvell-ci-update-Python-shebangs-to-python3.patch \
"

STABLE = "-stable"
BRANCH = "23.11"
SRCREV = "003b564aad38aa1453570ff7bfa7d76fe0d22633"
S = "${WORKDIR}/git"

def get_cpu_instruction_set(bb, d):
    import re
    march = re.search(r'-march=([^\s]*)', d.getVar('CC'))
    if march:
        return march.group(1)
    else:
        return "core2"

EXTRA_OEMESON = " -Dexamples=all -Dcpu_instruction_set=${@get_cpu_instruction_set(bb, d)} "

COMPATIBLE_MACHINE = "null"
COMPATIBLE_HOST:libc-musl:class-target = "null"
COMPATIBLE_HOST:linux-gnux32 = "null"

PACKAGECONFIG ??= " "
PACKAGECONFIG[afxdp] = ",,libbpf xdp-tools"
PACKAGECONFIG[libvirt] = ",,libvirt"

RDEPENDS:${PN} += "pciutils python3-core"
RDEPENDS:${PN}-examples += "bash"
DEPENDS = "numactl python3-pyelftools-native"

inherit meson pkgconfig

INSTALL_PATH = "${prefix}/share/dpdk"

do_install:append(){
    # remove  source files
    rm -rf ${D}/${INSTALL_PATH}/examples/*

    # Install examples
    install -m 0755 -d ${D}/${INSTALL_PATH}/examples/
    for dirname in ${B}/examples/dpdk-*
    do
        if [ ! -d ${dirname} ] && [ -x ${dirname} ]; then
            install -m 0755 ${dirname} ${D}/${INSTALL_PATH}/examples/
        fi
    done

}

PACKAGES =+ "${PN}-examples ${PN}-tools"

FILES:${PN} += " ${bindir}/dpdk-testpmd \
		 ${bindir}/dpdk-proc-info \
		 ${libdir}/*.so* \
		 ${libdir}/dpdk/pmds-24.0/*.so* \
		 "
FILES:${PN}-examples = " \
	${prefix}/share/dpdk/examples/* \
	"

FILES:${PN}-tools = " \
    ${bindir}/dpdk-pdump \
    ${bindir}/dpdk-test \
    ${bindir}/dpdk-test-* \
    ${bindir}/dpdk-*.py \
    "

CVE_PRODUCT = "data_plane_development_kit"

INSANE_SKIP:${PN} = "dev-so"
DEFAULT_PREFERENCE = "-1"
