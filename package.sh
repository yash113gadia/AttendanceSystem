#!/bin/bash
# Package script for Attendance Management System

APP_NAME="AttendEase"
VERSION="1.0.0"
DIST_DIR="dist"
JAR_NAME="$APP_NAME.jar"

echo "📦 Packaging $APP_NAME v$VERSION..."

# 1. Clean and Build
./build.sh

# 2. Prepare Dist Directory
rm -rf $DIST_DIR
mkdir -p $DIST_DIR

# 3. Create Manifest
mkdir -p bin/META-INF
echo "Main-Class: AttendanceSystem.MainGUI" > bin/META-INF/MANIFEST.MF

# 4. Create JAR
echo "🗜️  Creating JAR file..."
jar cfm $DIST_DIR/$JAR_NAME bin/META-INF/MANIFEST.MF -C bin .

# 5. Copy Resources
echo "📂 Copying resources..."
cp -r Logo $DIST_DIR/
cp README.md $DIST_DIR/
cp attendance_data.txt $DIST_DIR/ 2>/dev/null || touch $DIST_DIR/attendance_data.txt
cp users_data.txt $DIST_DIR/ 2>/dev/null || touch $DIST_DIR/users_data.txt
cp timetables_data.txt $DIST_DIR/ 2>/dev/null || touch $DIST_DIR/timetables_data.txt

# 6. Create Run Script for Distribution
echo "#!/bin/bash" > $DIST_DIR/start.sh
echo "java -jar $JAR_NAME" >> $DIST_DIR/start.sh
chmod +x $DIST_DIR/start.sh

# 7. Zip it up
echo "🤐 Zipping distribution..."
cd dist
zip -r ../${APP_NAME}_v${VERSION}.zip ./*
cd ..

echo "✅ Packaging Complete!"
echo "Distribution file: ${APP_NAME}_v${VERSION}.zip"
