#!/bin/bash
# React Frontend Build Script
## Mac/Linux

# Build the React application
echo "Building React frontend..."
cd frontend
npm run build

if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Copy build files to Spring Boot static folder
    echo "Copying build files to Spring Boot static folder..."
    
    sourceFolder="./build"
    destFolder="../src/main/resources/static"
    
    # Remove existing static files
    if [ -d "$destFolder" ]; then
        rm -rf "$destFolder"
    fi
    
    # Create new static folder and copy files
    mkdir -p "$destFolder"
    cp -r "$sourceFolder"/* "$destFolder"
    
    echo "Files copied to $destFolder"
    echo "React frontend is ready for Spring Boot!"
else
    echo "Build failed!"
    exit 1
fi

cd ..
