#!/bin/sh

cd ../ 
./gradlew :core:createDistributable

# AppDir structure according to https://github.com/AppImage/AppImageKit/wiki/AppDir
cp -r "core/build/compose/binaries/main/app/Kanji Dojo/" "AppImage/AppDir/usr"
cp "AppImage/AppDir/usr/lib/Kanji Dojo.png" "AppImage/AppDir/Kanji Dojo.png"

cd AppImage/
wget https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage
chmod +x ./appimagetool-x86_64.AppImage
./appimagetool-x86_64.AppImage AppDir/


