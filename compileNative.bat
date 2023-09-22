call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"
native-image -march=native --enable-preview --add-modules jdk.incubator.vector -jar target\com.tugalsan.gvm.cloud-1.0-SNAPSHOT-jar-with-dependencies.jar com.tugalsan.gvm.cloud
