# Producing useful obfuscated stack traces.
# See: http://proguard.sourceforge.net/manual/examples.html#stacktrace
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keep public class com.coiney.android.trueblue.* {
    public *;
}

-keep public interface com.coiney.android.trueblue.* {
    public *;
}

-keep public enum com.coiney.android.trueblue.* {
    **[] $VALUES;
    public *;
}
