<div align="center">

  <img src="preview_assets//inkscape_icon.svg" height=120 style="border-radius: 20px;">

  # Kanji Dojo
  ![Version Badge](https://img.shields.io/badge/version-v2.1.4-blue?style=for-the-badge&labelColor=ffffff&color=ff5555)

</div>

## Table of Contents
- [About this app](#about-this-app)
- [Screenshots](#screenshots)
- [Downloads and Installation](#downloads)
  * [Android](#android)
  * [Desktop (Windows, Linux, MacOS)](#desktop)
- [Version Comparison](#version-comparison)
- [Contributions](#contributions)
- [Technical Details](#technical-details)
- [Credits](#credits)

## About this app
Practice writing Japanese letters, learn their meanings and related words 

### Features
- Memorize how to write and read kana and kanji
- Follow JLPT levels or school grades
- Create your own decks to study, more than 6000 characters in total are available
- Use SRS reviewing system to avoid forgetting learned information
- Search letters and words with built-in dictionary
- Study words with flashcards
- Available modes to write words and pick correct letter readings
- Works offline

Repository with dictionary data and parsers here: [Kanji-Dojo-Data](https://github.com/syt0r/Kanji-Dojo-Data)

## Screenshots
<p float="left">
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" height="400"/>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" height="400"/>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" height="400"/>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" height="400"/>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" height="400"/>
<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" height="400"/>
</p>

## Downloads
### Android
[![Play Store](https://img.shields.io/badge/Google_Play-414141?style=for-the-badge&logo=google-play&logoColor=white)](https://play.google.com/store/apps/details?id=ua.syt0r.kanji)
[![F-Droid](https://img.shields.io/badge/F--Droid-1976D2?style=for-the-badge&logo=f-droid&logoColor=white)](https://f-droid.org/en/packages/ua.syt0r.kanji.fdroid/)

1. Available in [F-Droid](https://f-droid.org/en/packages/ua.syt0r.kanji.fdroid/), the same version is published in [GitHub Releases](https://github.com/syt0r/Kanji-Dojo/releases/latest)
2. [Google Play](https://play.google.com/store/apps/details?id=ua.syt0r.kanji)

### MacOS
1. Download `.dmg` installer from [Github Releases](https://github.com/syt0r/Kanji-Dojo/releases) for your platform
   - `kanji-dojo-arm-*.dmg` for Apple Silicon
   - `kanji-dojo-intel-*.dmg` for devices with older Intel CPU
2. Install the app
   - Go to `Privacy & Security` screen in system settings and click on `Open Anyway` button under security section for `Kanji Dojo.app`

### Windows & Linux
1. Install Java 17 or higher for your platform
2. Download jar from [Github Releases](https://github.com/syt0r/Kanji-Dojo/releases) for your platform
3. Run downloaded jar with double click or use command ```java -jar <filename>.jar```

## Version comparison
| Google Play                                                                         | F-Droid | Desktop |
|-------------------------------------------------------------------------------------|---------|---------|
| Firebase Analytics </br>Play services for in app review </br> Billing for donations | -       | -       |

## Contributions
- Pull-Requests are welcome!
- Before making PR create and discuss your issue
- Use proper code formatting

### Technical details
Used Libraries: `Compose Multiplatform`, `Kotlin Coroutines`, `Koin`, `SqlDelight`

### Credits
* **KanjiVG**</br>
  Provides writing strokes, radicals information </br>
  License: Creative Commons Attribution-Share Alike 3.0</br>
  Link: https://kanjivg.tagaini.net/
* **Kanji Dic**</br>
  Provides characters info, such as meanings, readings and classifications </br>
  License: Creative Commons Attribution-Share Alike 3.0</br>
  Link: http://www.edrdg.org/wiki/index.php/KANJIDIC_Project
* **Tanos by Jonathan Waller**</br>
  Provides JLPT classification for kanji </br>
  License: Creative Commons BY</br>
  Link: http://www.tanos.co.uk/jlpt/
* **JMDict**</br>
  Japanese-Multilingual dictionary, provides expressions </br>
  License: Creative Commons Attribution-Share Alike 4.0</br>
  Link: https://www.edrdg.org/jmdict/j_jmdict.html
* **JmdictFurigana**</br>
  Open-source furigana resource to complement the EDICT/Jmdict and ENAMDICT/Jmnedict dictionary files </br>
  License: Creative Commons Attribution-Share Alike 4.0</br>
  Link: https://github.com/Doublevil/JmdictFurigana
* **Frequency list by Leeds university**</br>
  Words ranking by frequency of usage in internet </br>
  License: Creative Commons BY</br>
  Link: http://corpus.leeds.ac.uk/list.html
* **yomichan-jlpt-vocab**</br>
  This meta dictionary adds JLPT-level tags to words in Yomichan. Provides associations between JLPT words from Tanos and JMDict</br>
  License: Creative Commons Attribution-Share Alike 4.0</br>
  Link: https://github.com/stephenmk/yomichan-jlpt-vocab</br>

### License
> (c) 2022-2023 Yaroslav Shuliak
> 
> This is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
> 
> This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
> 
> You should have received a copy of the GNU General Public License along with this app. If not, see https://www.gnu.org/licenses/.
