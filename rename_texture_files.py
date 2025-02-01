import os
import re

import os
import re
import sys

def rename_files(directory="src/main/resources/textures"):
    # Compile a regex pattern to extract the relevant parts of the filename
    pattern = re.compile(r"minecraft__(.*?)__\{.*?potion__'minecraft__(.*?)'\}.*?\.png")

    # Check if the directory exists
    if not os.path.exists(directory):
        print(f"Directory '{directory}' does not exist.")
        return

    # Iterate over all files in the specified directory
    for filename in os.listdir(directory):
        # Check if the filename contains the '{' character
        if '{' in filename:
            # Match the filename against the regex pattern
            match = pattern.match(filename)
            if match:
                # Extract the relevant parts of the filename
                item_type = match.group(1)
                potion_type = match.group(2)
                
                # Create the new filename
                new_filename = f"minecraft__{item_type}__{potion_type}.png"
                
                # Get the full paths for the old and new filenames
                old_file_path = os.path.join(directory, filename)
                new_file_path = os.path.join(directory, new_filename)
                
                # Rename the file
                os.rename(old_file_path, new_file_path)
                print(f"Renamed '{filename}' to '{new_filename}'")
            else:
                print(f"Skipping file '{filename}' as it does not match the expected pattern")

# Allow passing the directory as a command-line argument
if __name__ == "__main__":
    # Check if a directory argument was provided
    if len(sys.argv) > 1:
        directory = sys.argv[1]

    # Call the function to rename the files
    rename_files() # Default path: src/main/resources/textures