function isRemappingEnabledForClass(node) {
    var flag1 = node.name.startsWith("net/mirolls/melodyskyplus/client/AntiBug");
    var flag3 = node.name.startsWith("net/mirolls/melodyskyplus/modules/MelodyPlusModules");
    var flag4 = node.name.startsWith("net/mirolls/melodyskyplus/Verify");
    var flag5 = node.name.startsWith("net/mirolls/melodyskyplus/client/Bug");

    var path = node.name.startsWith("net/mirolls/melodyskyplus/path");
    return flag1 || flag3 || flag4 || flag5 || path;
}
function isObfuscatorEnabledForClass(node) {
    var flag1 = node.name.startsWith("net/mirolls/melodyskyplus/client/AntiBug");
    var flag3 = node.name.startsWith("net/mirolls/melodyskyplus/modules/MelodyPlusModules");
    var flag4 = node.name.startsWith("net/mirolls/melodyskyplus/Verify");
    var flag5 = node.name.startsWith("net/mirolls/melodyskyplus/client/Bug");

    var path = node.name.startsWith("net/mirolls/melodyskyplus/path");
    return flag1 || flag3 || flag4 || flag5 || path;
}