function isRemappingEnabledForClass(node) {
    var flag1 = node.name.startsWith("net/mirolls/melodyskyplus/client/AntiBug");
    var flag2 = node.name.startsWith("net/mirolls/melodyskyplus/client/AntiRat");
    var flag3 = node.name.startsWith("net/mirolls/melodyskyplus/modules/MelodyPlusModules");
    var flag4 = node.name.startsWith("net/mirolls/melodyskyplus/Verify");
    var flag5 = node.name.startsWith("net/mirolls/melodyskyplus/client/Bug");
    return flag1 || flag2 || flag3 || flag4 || flag5;
}
function isObfuscatorEnabledForClass(node) {
    var flag1 = node.name.startsWith("net/mirolls/melodyskyplus/client/AntiBug");
    var flag2 = node.name.startsWith("net/mirolls/melodyskyplus/client/AntiRat");
    var flag3 = node.name.startsWith("net/mirolls/melodyskyplus/modules/MelodyPlusModules");
    var flag4 = node.name.startsWith("net/mirolls/melodyskyplus/Verify");
    var flag5 = node.name.startsWith("net/mirolls/melodyskyplus/client/Bug");
    return flag1 || flag2 || flag3 || flag4 || flag5;
}