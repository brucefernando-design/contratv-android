# Para compilar tu APK automáticamente en la nube (GitHub Actions):
# 1. Ve a tu repositorio: https://github.com/brucefernando-design/contratv-android
# 2. Haz clic en "Add file" -> "Create new file"
# 3. En el nombre escribe: .github/workflows/build-apk.yml
# 4. Pega el siguiente contenido y haz clic en "Commit changes...":

name: Build ContraTV APK

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout Source Code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Set up Android SDK
      uses: android-actions/setup-android@v3

    - name: Build APK with Gradle
      run: |
        gradle wrapper --gradle-version 8.7
        chmod +x gradlew
        ./gradlew assembleDebug --stacktrace
        cp app/build/outputs/apk/debug/app-debug.apk ContraTV.apk

    - name: Upload APK Artifact
      uses: actions/upload-artifact@v4
      with:
        name: ContraTV-APK
        path: ContraTV.apk

    - name: Create Release with Download Link
      uses: softprops/action-gh-release@v2
      with:
        tag_name: v1.0.0
        name: ContraTV v1.0.0 Oficial
        body: APK Oficial de ContraTV para Android TV, Fire Stick, Onn TV, Cajas Chinas y Móviles.
        files: ContraTV.apk
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
