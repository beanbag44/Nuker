#!/bin/bash
if [ $# -eq 0 ]
  then
    echo "Usage: ./release_build.sh <version>"
    echo "The <version> should be in the format x.y.z"
    exit 1
fi

# Set the new version
NEW_VERSION=$1

# Check if the gradle.properties file exists
if [ ! -f "gradle.properties" ]; then
    echo "Error: gradle.properties file not found."
    exit 1
fi

# Update the version in gradle.properties
sed -i "s/version=.*/version=$NEW_VERSION/" gradle.properties

# Add the changes to git
git add gradle.properties

# Commit the changes
git commit -m "Version updated to $NEW_VERSION"

# Push the changes
git push

# Add a tag
git tag -a "$NEW_VERSION" -m "Version $NEW_VERSION"

# Push the tag
git push --tags

echo "New version released: $NEW_VERSION"