#!/bin/bash
if [ $# -eq 0 ] || [ -z "$2" ];
  then
    echo "Usage: ./release_build.sh <version> <release message>"
    echo "The <version> should be in the format x.y.z"
    echo "The <release message> should not have quotes and can contain new line characters"
    exit 1
fi

# Set the new version
NEW_VERSION=$1
RELEASE_MESSAGE=${@/$NEW_VERSION/}
# Check if the gradle.properties file exists
if [ ! -f "gradle.properties" ]; then
    echo "Error: gradle.properties file not found."
    exit 1
fi

# Update the version in gradle.properties
sed -i '' "s/mod_version=.*/mod_version=$NEW_VERSION/" gradle.properties

# Add the changes to git
git add gradle.properties

# Commit the changes
git commit -m "Version updated to $NEW_VERSION \n$RELEASE_MESSAGE"

# Push the changes
git push

# Add a tag
git tag -a "$NEW_VERSION" -m "Version $NEW_VERSION"

# Push the tag
git push origin "$NEW_VERSION"

echo "New version released: $NEW_VERSION"