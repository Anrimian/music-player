-renamesourcefileattribute SourceFile
-printmapping out.map


-keepclassmembers class com.github.anrimian.musicplayer.lite.di.LiteComponents {
    public void init(android.content.Context);
    public getLiteAppComponent();
}