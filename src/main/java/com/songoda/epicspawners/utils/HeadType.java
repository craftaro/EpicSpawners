package com.songoda.epicspawners.utils;

import org.bukkit.entity.EntityType;

/**
 * Created by songoda on 3/20/2017.
 */
public enum HeadType {

    BAT("http://textures.minecraft.net/texture/978862a56119227aaad4b7c246c8b2256db985db0951f55b0a1f8616c191f"),
    BLAZE("http://textures.minecraft.net/texture/b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0"),
    CHICKEN("http://textures.minecraft.net/texture/1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893"),
    COW("http://textures.minecraft.net/texture/5d6c6eda942f7f5f71c3161c7306f4aed307d82895f9d2b07ab4525718edc5"),
    CREEPER("http://textures.minecraft.net/texture/295ef836389af993158aba27ff37b6567185f7a721ca90fdfeb937a7cb5747"),
    CAVE_SPIDER("http://textures.minecraft.net/texture/41645dfd77d09923107b3496e94eeb5c30329f97efc96ed76e226e98224"),
    DONKEY("http://textures.minecraft.net/texture/9ffca9f0859834da81af524c2865fa3186b5bf58bd1c4e3742837c4fd6853887"),
    ELDER_GUARDIAN("http://textures.minecraft.net/texture/dbb9491b7a1768e3291959796232eb6f61b9c39aa2f81e13d287a3ab5d16dec"),
    ENDERMAN("http://textures.minecraft.net/texture/7a59bb0a7a32965b3d90d8eafa899d1835f424509eadd4e6b709ada50b9cf"),
    ENDERMITE("http://textures.minecraft.net/texture/e8c6cb8ceaad5a2ad5cc9a67bce6d5bdbf5cbb7e312955ccf9f162509355b1"),
    ENDER_DRAGON("http://textures.minecraft.net/texture/74ecc040785e54663e855ef0486da72154d69bb4b7424b7381ccf95b095a"),
    EVOKER("http://textures.minecraft.net/texture/bb48ed634196b7ded7455fdde56f3fa9f51b28f7b73839f0c6783b3961d"),
    GHAST("http://textures.minecraft.net/texture/8b6a72138d69fbbd2fea3fa251cabd87152e4f1c97e5f986bf685571db3cc0"),
    GIANT("http://textures.minecraft.net/texture/56fc854bb84cf4b7697297973e02b79bc10698460b51a639c60e5e417734e11"),
    GUARDIAN("http://textures.minecraft.net/texture/932c24524c82ab3b3e57c2052c533f13dd8c0beb8bdd06369bb2554da86c123"),
    HORSE("http://textures.minecraft.net/texture/61902898308730c4747299cb5a5da9c25838b1d059fe46fc36896fee662729"),
    HUSK("http://textures.minecraft.net/texture/6ae3a5bfcaa943d126988ed17ce8e4a7fb4231b05a5c5c947e5cc9752a199962"),
    ILLUSIONER("http://textures.minecraft.net/texture/c269a9dabe4f9fd6b4d74b7cd9c7ee6b24d87549b5acabbeb36cd756cc373"),
    IRON_GOLEM("http://textures.minecraft.net/texture/89091d79ea0f59ef7ef94d7bba6e5f17f2f7d4572c44f90f76c4819a714"),
    LLAMA("http://textures.minecraft.net/texture/6bc438fc1fbbaea2289aabecedd3fdf269ddc979bf8b5c6a8fc4bb8dcd4e1fe"),
    MAGMA_CUBE("http://textures.minecraft.net/texture/38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429"),
    MULE("http://textures.minecraft.net/texture/36fcd3ec3bc84bafb4123ea479471f9d2f42d8fb9c5f11cf5f4e0d93226"),
    MUSHROOM_COW("http://textures.minecraft.net/texture/d0bc61b9757a7b83e03cd2507a2157913c2cf016e7c096a4d6cf1fe1b8db"),
    OCELOT("http://textures.minecraft.net/texture/5657cd5c2989ff97570fec4ddcdc6926a68a3393250c1be1f0b114a1db1"),
    OMNI("http://textures.minecraft.net/texture/afe097f7f7a6568884198b562cb58cfb84a3936fbf72828954aaa1f58cba32"),
    PARROT("http://textures.minecraft.net/texture/fd7cca30a1dc2fb25d3b7fb16318330ac79742d97deeeabe5393fb8a2cb878"),
    PIG("http://textures.minecraft.net/texture/621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4"),
    PIG_ZOMBIE("http://textures.minecraft.net/texture/74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb"),
    POLAR_BEAR("http://textures.minecraft.net/texture/d46d23f04846369fa2a3702c10f759101af7bfe8419966429533cd81a11d2b"),
    RABBIT("http://textures.minecraft.net/texture/dc7a317ec5c1ed7788f89e7f1a6af3d2eeb92d1e9879c05343c57f9d863de130"),
    SHEEP("http://textures.minecraft.net/texture/f31f9ccc6b3e32ecf13b8a11ac29cd33d18c95fc73db8a66c5d657ccb8be70"),
    SHULKER("http://textures.minecraft.net/texture/1433a4b73273a64c8ab2830b0fff777a61a488c92f60f83bfb3e421f428a44"),
    SILVERFISH("http://textures.minecraft.net/texture/453a6aedbc9e4a22266cd70d2d4a2850a7e4b5864fbcbddf952b748fbabdb2"),
    SKELETON("http://textures.minecraft.net/texture/2e5be6a3c0159d2c1f3b1e4e1d8384b6f7ebac993d58b10b9f8989c78a232"),
    SKELETON_HORSE("http://textures.minecraft.net/texture/39be68887f5cf8c8044ebcdc5e768a172b2ef3e99545acd75b5ff74a263a"),
    SLIME("http://textures.minecraft.net/texture/16ad20fc2d579be250d3db659c832da2b478a73a698b7ea10d18c9162e4d9b5"),
    SNOWMAN("http://textures.minecraft.net/texture/a528df1653962e4e995dfd064a72b2cbff9b7197912880143f941a37db46c"),
    SPIDER("http://textures.minecraft.net/texture/cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1"),
    SQUID("http://textures.minecraft.net/texture/01433be242366af126da434b8735df1eb5b3cb2cede39145974e9c483607bac"),
    STRAY("http://textures.minecraft.net/texture/96e58aeeaf75e9695813aeaabd3973c83d5ff8c285fe6b944550bc41b953"),
    VEX("http://textures.minecraft.net/texture/6dc4b59be077f7f2f6b488666afb85e45d96cc57b57934ae11ba9734bb4811"),
    VILLAGER("http://textures.minecraft.net/texture/822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b"),
    VINDICATOR("http://textures.minecraft.net/texture/7a5b31aeab7f728cd7e90f81e2ffad4cd198c1907574f57be2b7c42f49ddd"),
    WITCH("http://textures.minecraft.net/texture/2e139130d7efd41fbad53735f64f8aff265bd7c54977189c02babbec4b0d07b"),
    WITHER("http://textures.minecraft.net/texture/cdf74e323ed41436965f5c57ddf2815d5332fe999e68fbb9d6cf5c8bd4139f"),
    WITHER_SKELETON("http://textures.minecraft.net/texture/233b41fa79cd53a230e2db942863843183a70404533bbc01fab744769bcb"),
    WOLF("http://textures.minecraft.net/texture/e95cbb4f75ea87617f2f713c6d49dac3209ba1bd4b9369654b1459ea15317"),
    ZOMBIE("http://textures.minecraft.net/texture/56fc854bb84cf4b7697297973e02b79bc10698460b51a639c60e5e417734e11"),
    ZOMBIE_HORSE("http://textures.minecraft.net/texture/d898e3eacff9949a9de9777ddfada8a7f62a4102de47b54db35f9f843e1ce4"),
    ZOMBIE_VILLAGER("http://textures.minecraft.net/texture/1a207a2b872fe8dd22ce5e27fc3263622621635efd8ea6abbdf317f99c5843"),
    TURTLE("http://textures.minecraft.net/texture/8df85c60a2e6061a5b4100e224422d19373badae2c0a1ca13e4d94897f7cbcf3"),
    PHANTOM("http://textures.minecraft.net/texture/fc060c4c1565f337a1b399ccf247cf300d3c3843efaaa610e5d894a79d9957fe"),
    DOLPHIN("http://textures.minecraft.net/texture/546fd34b06c61f68e94f357f38e62d334e339edf2b930d26951971921bbb5268"),
    PUFFERFISH("http://textures.minecraft.net/texture/13bc144030373790ca5a03bf111ca4127d31729bf0788d1e05a019206e62da92"),
    SALMON("http://textures.minecraft.net/texture/c1102e3a82c44ad4180e5be1cedda386158f5b8b20a52a8bd941104f7b695b9f"),
    COD("http://textures.minecraft.net/texture/a5eef52a468f681fab6a56bf5cc9986c1868ff0795d115c6ab0009d8faf972a4"),
    TROPICAL_FISH("http://textures.minecraft.net/texture/b80f5955825e822eff416182925c7d650a7e0947ec9f855a75450e03175e8338"),
    DROWNED("http://textures.minecraft.net/texture/71f2469f9b4bd92aa6a8ba3415f1ab76fd746041400062dc40fe13b646c0cc5d"),
    DROPPED_ITEM("http://textures.minecraft.net/texture/452fe4ce1f1d53a12ed443eeba7297e81da581e0c7a39954d9d7bba7de59c46"),
    WANDERING_TRADER("http://textures.minecraft.net/texture/4e8882cb316875e867b8d90c197f3ab57ff809fe8dd4a88368354afada48cdb4"),
    PANDA("http://textures.minecraft.net/texture/3cddc208575aadfdc47c1c707f8230b4775f70cec7531a9a355774f59e6bffa7"),
    CAT("http://textures.minecraft.net/texture/ff1b549341ad53b2bcd851b224ec56dbadb54887eaa0f771ba8eb39f58cf1d59"),
    FOX("http://textures.minecraft.net/texture/1528ee714d3fca31655feae1cd3c14d346f9bcae3e457b7658c9f915e21"),
    PILLAGER("http://textures.minecraft.net/texture/63550f89aea88665804c2fd1b6682930c23233184fdc060991305718ed660597"),
    RAVAGER("http://textures.minecraft.net/texture/d91a9d864e36fbec19b584bcb71292530755b10c4e60784fec92b51f8189363c"),
    TRADER_LLAMA("http://textures.minecraft.net/texture/6bc438fc1fbbaea2289aabecedd3fdf269ddc979bf8b5c6a8fc4bb8dcd4e1fe"),
    BEE("http://textures.minecraft.net/texture/5162dd0b9f65b58a1e70f81d8e03e8ff6c53e4e985bdbe0186558d8a69a81189"),
    PIGLIN("http://textures.minecraft.net/texture/8ed4c6c5e8ba2c3ab05cb47258a4fe95bd3b92ba0a68db6d6e961d830baa0179"),
    HOGLIN("http://textures.minecraft.net/texture/6c8c7fb74e69885f294ca6652553dd5938dac1164f652d4843abe0e891453da4"),
    STRIDER("http://textures.minecraft.net/texture/16a9186a317e31e77b85a625f9d0b1cf1809f9d46a4aa153e639b60d30743fcf"),
    ZOMBIFIED_PIGLIN("http://textures.minecraft.net/texture/f05e06b1d1357a3574268a5189615983ceffc185993047da47ac947f3b7507f3");

    private final String url;

    private HeadType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

}
