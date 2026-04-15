# React Frontend Build Script
## Windows PowerShell

# Build the React application
Write-Host "Building React frontend..." -ForegroundColor Green
cd frontend
npm run build

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful!" -ForegroundColor Green
    
    # Copy build files to Spring Boot static folder
    Write-Host "Copying build files to Spring Boot static folder..." -ForegroundColor Green
    
    $sourceFolder = ".\build\*"
    $destFolder = "..\src\main\resources\static"
    
    # Remove existing static files
    if (Test-Path $destFolder) {
        Remove-Item $destFolder -Recurse -Force
    }
    
    # Create new static folder and copy files
    New-Item -ItemType Directory -Path $destFolder -Force | Out-Null
    Copy-Item $sourceFolder -Destination $destFolder -Recurse -Force
    
    Write-Host "Files copied to $destFolder" -ForegroundColor Green
    Write-Host "React frontend is ready for Spring Boot!" -ForegroundColor Green
} else {
    Write-Host "Build failed!" -ForegroundColor Red
}

cd ..
