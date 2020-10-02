# Install NDK for linux with given version
function install_ndk_linux() {
  NDK_VERSION=$1
  FILE="android-ndk-$NDK_VERSION-linux-x86_64.zip"
  curl "https://dl.google.com/android/repository/android-ndk-$NDK_VERSION-linux-x86_64.zip" --output $FILE
  unzip $FILE
}

# Install NDK for Mac with given version
function install_ndk_mac() {
  NDK_VERSION=$1
  FILE="android-ndk-$NDK_VERSION-darwin-x86_64.zip"
  curl "https://dl.google.com/android/repository/android-ndk-$NDK_VERSION-darwin-x86_64.zip" --output $FILE
  unzip $FILE
}

# Check the existing NDK with given version if it already installed then return the absolute path
function check_ndk_path() {
  NDK_VERSION=$1
  echo $(find / -type d -name "android-ndk-$NDK_VERSION" -print -quit 2>/dev/null)
}

# Install OpenJDK8 for Mac then return the default installation path
function install_java8_mac() {
  FILE=OpenJDK8U-jdk_x64_mac_hotspot_8u265b01.pkg
  curl -L "https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u265-b01/OpenJDK8U-jdk_x64_mac_hotspot_8u265b01.pkg" --output $FILE
  sudo installer -pkg $FILE -target /
}
