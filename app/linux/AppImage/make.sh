#!/bin/bash

APP_IMAGE_DIR="app/linux/AppImage/"

if [ "$(basename "$PWD")" == "AppImage" ]; then
  echo "Running from AppImage folder"
  cd ../../../
elif [ -f "README.md" ]; then
  echo "Running from the repo's root"
else
  echo "Executed from an unsupported directory, aborting"
  exit
fi

./gradlew :core:createDistributable

# AppDir structure according to https://github.com/AppImage/AppImageKit/wiki/AppDir
cp -r "core/build/compose/binaries/main/app/Kanji Dojo/" "$APP_IMAGE_DIR/AppDir/usr"
cp "$APP_IMAGE_DIR/AppDir/usr/lib/Kanji Dojo.png" "$APP_IMAGE_DIR/AppDir/Kanji Dojo.png"

cd $APP_IMAGE_DIR

if [ ! -f "appimagetool-x86_64.AppImage" ]; then
  wget https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage
fi

chmod +x ./appimagetool-x86_64.AppImage
chmod +x AppDir/AppRun

./appimagetool-x86_64.AppImage AppDir/
chmod +x Kanji_Dojo-x86_64.AppImage
