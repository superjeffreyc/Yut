import math # Import the math module for trigonometric functions
import os
import sys
from PIL import Image, ImageDraw

def move_image_vertical(image_path, output_path, pixels_to_move, direction='up'):
    """
    Moves an image up or down by a specified number of pixels.

    Args:
        image_path (str): Path to the input image.
        output_path (str): Path to save the output image.
        pixels_to_move (int): Number of pixels to move the image.
        direction (str): 'up' to move up, 'down' to move down.
                         Defaults to 'up'.
    """
    try:
        img = Image.open(image_path)
    except FileNotFoundError:
        print(f"Error: Image not found at {image_path}")
        return

    width, height = img.size
    new_img = Image.new("RGBA", (width, height), (0, 0, 0, 0)) # Create a transparent new image

    if direction == 'up':
        # Move up: new_img.paste(img, (x_offset, y_offset))
        # To move up, the y_offset for pasting should be negative for the original image,
        # or positive for the original image when pasted onto a new canvas.
        # Since we're pasting onto a new canvas of the same size,
        # we effectively shift the image *within* that canvas.
        # So, to move the *content* up, we paste it at a negative y-offset
        # relative to the top of the new image.
        # Or, more intuitively, we cut off the top and add transparent pixels at the bottom.
        # However, a simpler way is to think about where the top-left corner of the
        # original image should be placed on the new image.
        # To move up, the top of the original image should be higher,
        # meaning its y-coordinate on the new canvas should be smaller.
        # But since coordinates start from 0 at the top, a smaller y-coordinate means
        # it's closer to the top.
        # Let's consider the new_img as our target.
        # If we paste at (0, -pixels_to_move), the image content will move up.
        new_img.paste(img, (0, -pixels_to_move))
    elif direction == 'down':
        # Move down:
        # To move down, the top of the original image should be lower,
        # meaning its y-coordinate on the new canvas should be larger.
        new_img.paste(img, (0, pixels_to_move))
    else:
        print("Invalid direction. Use 'up' or 'down'.")
        return

    new_img.save(output_path)
    print(f"Image moved {pixels_to_move} pixels {direction} and saved to {output_path}")


def create_copies(
    original_bird_path,
    output_path,
    background_size=(400, 400), # Size of the final image canvas
    animal_placements=[]
):
    """
    Shrinks the bird image and places multiple copies at specified offsets.

    Args:
        original_bird_path (str): Path to the original bird image (e.g., 'bird3jump3.png').
        output_path (str): Path to save the final composite image.
        background_size (tuple): (width, height) of the canvas for the flock.
        background_color (tuple): RGB color for the background (e.g., sky).
        animal_placements (list of dict): A list of dictionaries, each defining
                                      how to place a bird:
                                      [{'scale': float, 'position': (x, y)}, ...]
                                      'scale': e.g., 0.5 for 50% of original size.
                                      'position': (x, y) top-left corner on the canvas.
    """
    try:
        # Open the original bird image. Ensure it's RGBA for transparency.
        original_bird = Image.open(original_bird_path).convert("RGBA")
    except FileNotFoundError:
        print(f"Error: Original bird image not found at {original_bird_path}")
        return

    # Create the background canvas
    # Use 'RGBA' mode for the background too if you want the final image to have transparency
    # (though in this case we're filling with a color, so 'RGB' is fine unless you export to PNG)
    output_image = Image.new("RGBA", background_size, (0, 0, 0, 0))

    for config in animal_placements:
        scale = config.get('scale', 1.0) # Default to no scaling
        position = config.get('position', (0, 0)) # Default to top-left

        # Calculate new size based on scale
        new_width = int(original_bird.width * scale)
        new_height = int(original_bird.height * scale)

        # Ensure dimensions are at least 1x1 to avoid errors
        new_width = max(1, new_width)
        new_height = max(1, new_height)

        # Shrink the bird image
        # Use Image.LANCZOS for high-quality downsampling
        shrunk_bird = original_bird.resize((new_width, new_height), Image.LANCZOS)

        # Paste the shrunk bird onto the output image
        # The third argument (shrunk_bird itself) serves as the mask,
        # ensuring transparency is handled correctly.
        output_image.paste(shrunk_bird, position, shrunk_bird)
        print(f"Pasted bird at {position} with size {new_width}x{new_height}")

    output_image.save(output_path)
    print(f"Combined bird image saved to {output_path}")

def place_medal_on_animal(animal_image_path, medal_image_path, output_image_path, medal_size=None, position=(0, 0)):
    """
    Places a medal image on top of an animal image.

    Args:
        animal_image_path (str): Path to the animal image file.
        medal_image_path (str): Path to the medal image file.
        output_image_path (str): Path to save the combined image.
        medal_size (tuple, optional): A tuple (width, height) to resize the medal.
                                      If None, the medal's original size is used.
        position (tuple, optional): A tuple (x, y) for the top-left corner
                                    where the medal will be placed on the animal.
    """
    try:
        animal_img = Image.open(animal_image_path).convert("RGBA")
        medal_img = Image.open(medal_image_path).convert("RGBA")
    except FileNotFoundError:
        print("Error: One or both image files not found. Please check paths.")
        return

    # Resize the medal if a size is specified
    if medal_size:
        medal_img = medal_img.resize(medal_size, Image.Resampling.LANCZOS)

    # Calculate the position for the medal (center it horizontally on the animal's chest for example)
    # This is a basic example, you might need to adjust 'position' for precise placement.
    if position == (0, 0): # If no specific position is given, try to center it reasonably
        # A rough estimate to place it on the chest area
        x = (animal_img.width - medal_img.width) // 2
        y = int(animal_img.height * 0.5) # Adjust this multiplier for vertical placement
        position = (x, y)

    # Paste the medal onto the animal image
    # The last argument (medal_img) acts as a mask, using its alpha channel for transparency.
    animal_img.paste(medal_img, position, medal_img)

    # Save the combined image
    animal_img.save(output_image_path)
    print(f"Combined image saved to: {output_image_path}")

def create_circular_animal_icon(
    animal_image_path,
    output_path,
    circle_padding=0,  # Padding (in pixels) between the image edge and the inner part of the circle
    border_thickness=20 # Thickness (in pixels) of the blue border
):
    """
    Takes an animal image, crops it into a circle, makes the outside transparent,
    and then draws a blue border around the circular content.

    Args:
        animal_image_path (str): Path to the animal image (e.g., 'seal1jump1.png').
        output_path (str): Path to save the final composite image.
        circle_padding (int): The number of pixels from the image's shortest edge
                              to the start of the animal content circle. This controls
                              the inner radius of the final icon.
        border_thickness (int): The thickness of the blue border in pixels.
    """
    try:
        # Load the animal image and ensure it has an alpha channel for transparency
        animal_img = Image.open(animal_image_path).convert("RGBA")
    except FileNotFoundError:
        print("Error: Image file not found. Please ensure it is in the correct directory.")
        return

    img_width, img_height = animal_img.size

    # --- 1. Create a circular mask for the animal image content ---
    # This mask will define which parts of the animal_img remain visible.
    mask = Image.new("L", (img_width, img_height), 0) # Create a black (transparent) grayscale mask
    draw_mask = ImageDraw.Draw(mask)

    # Calculate the bounding box for the inner circle of the mask.
    # We want this circle to be centered and leave space for padding and the border.
    # The diameter of this inner circle will be determined by the shortest dimension
    # of the animal image minus twice the padding and twice the border thickness.

    # Calculate the diameter for the actual animal content
    content_diameter = min(img_width, img_height) - (2 * (circle_padding + border_thickness))

    # Ensure diameter is positive to avoid errors
    if content_diameter <= 0:
        print("Error: content_diameter is too small or negative. Adjust circle_padding or border_thickness.")
        return

    # Calculate the top-left and bottom-right coordinates for the content circle
    content_circle_left = (img_width - content_diameter) / 2
    content_circle_upper = (img_height - content_diameter) / 2
    content_circle_right = content_circle_left + content_diameter
    content_circle_lower = content_circle_upper + content_diameter

    # Draw a filled white circle on the mask. White areas (255) in the mask
    # correspond to opaque areas in the original image.
    draw_mask.ellipse(
        (content_circle_left, content_circle_upper, content_circle_right, content_circle_lower),
        fill=255
    )

    # Apply this mask to the animal image.
    # This makes all parts of animal_img outside the mask's white circle transparent.
    animal_img.putalpha(mask)

    # --- 2. Get the blue color from the marker image's border ---
    blue_color = (85, 173, 191) # A common blue similar to your marker (RGB)

    # --- 3. Draw the blue border on the now-circular animal image ---
    draw_final = ImageDraw.Draw(animal_img)

    # Calculate the bounding box for the outer edge of the blue border.
    # This circle will sit around the transparent-masked animal content.
    border_outer_diameter = min(img_width, img_height) - (2 * circle_padding)
    border_outer_left = (img_width - border_outer_diameter) / 2
    border_outer_upper = (img_height - border_outer_diameter) / 2
    border_outer_right = border_outer_left + border_outer_diameter
    border_outer_lower = border_outer_upper + border_outer_diameter

    # Draw the blue circle as an outline.
    draw_final.ellipse(
        (border_outer_left, border_outer_upper, border_outer_right, border_outer_lower),
        outline=blue_color,
        width=border_thickness
    )

    # Save the final image. Use PNG to preserve transparency.
    animal_img.save(output_path)
    print(f"Circular animal icon with blue border saved to {output_path}")

def convert_transparent_to_white(input_image_path, output_image_path):
    """
    Converts transparent pixels in an image to white pixels.

    Args:
        input_image_path (str): Path to the input image file (e.g., PNG with transparency).
        output_image_path (str): Path to save the output image file.
    """
    try:
        # Open the image and ensure it has an alpha channel (RGBA)
        # If the image doesn't have an alpha channel, this will add one (with full opacity)
        # If it already has one, it will preserve it.
        img = Image.open(input_image_path).convert("RGBA")
    except FileNotFoundError:
        print(f"Error: Input image file not found at {input_image_path}")
        return
    except Exception as e:
        print(f"Error opening or converting image: {e}")
        return

    # Create a new white background image with the same dimensions as the original
    # The 'RGB' mode is sufficient for the background as it won't have transparency
    white_background = Image.new("RGB", img.size, (255, 255, 255)) # (255, 255, 255) is RGB for white

    # Paste the original image onto the white background
    # The 'img' itself is passed as the mask, which tells paste() to use the
    # alpha channel of 'img' to determine transparency.
    # Where 'img' is opaque, its pixels will be used.
    # Where 'img' is transparent, the white_background pixels will remain.
    white_background.paste(img, (0, 0), img)

    try:
        # Save the resulting image
        # You can save it as JPEG if you don't need transparency anymore,
        # otherwise PNG is also fine.
        white_background.save(output_image_path)
        print(f"Image saved successfully to: {output_image_path}")
    except Exception as e:
        print(f"Error saving image: {e}")


def create_medal_image(width, height):
    """
    Creates an image of a medal with a blue ribbon and a gold star,
    with a transparent background.

    Args:
        width (int): The width of the image.
        height (int): The height of the image.

    Returns:
        PIL.Image.Image: The generated medal image with transparency.
    """
    # Create a new RGBA image with the specified width and height
    # RGBA mode includes an alpha channel for transparency.
    # Set initial color to (0, 0, 0, 0) for transparent black.
    img = Image.new('RGBA', (width, height), color = (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # --- Draw the ribbon (blue lines) FIRST, so the medal can overlap it ---
    ribbon_thickness = 20 # Width of the ribbon lines
    medal_radius = min(width, height) // 5
    medal_center_x = width // 2
    medal_center_y = height // 2 + 20 # Slightly lower to accommodate ribbon

    # Left ribbon segment
    # Start point (top-left)
    rl_x1 = medal_center_x - medal_radius - 50 # Further left from medal
    rl_y1 = medal_center_y - medal_radius - 80 # Higher up

    # End point (connecting near the top of the medal)
    rl_x2 = medal_center_x - 15 # Closer to medal center
    rl_y2 = medal_center_y - medal_radius + 5 # Just above the medal

    draw.line([(rl_x1, rl_y1), (rl_x2, rl_y2)], fill='blue', width=ribbon_thickness)

    # Right ribbon segment
    # Start point (top-right)
    rr_x1 = medal_center_x + medal_radius + 50 # Further right from medal
    rr_y1 = medal_center_y - medal_radius - 80 # Higher up

    # End point (connecting near the top of the medal)
    rr_x2 = medal_center_x + 15 # Closer to medal center
    rr_y2 = medal_center_y - medal_radius + 5 # Just above the medal

    draw.line([(rr_x1, rr_y1), (rr_x2, rr_y2)], fill='blue', width=ribbon_thickness)


    # --- Draw the medal (gold circle) SECOND, so it's on top of the ribbon ---
    # Define the bounding box for the circle
    medal_bbox = [
        (medal_center_x - medal_radius, medal_center_y - medal_radius),
        (medal_center_x + medal_radius, medal_center_y + medal_radius)
    ]
    draw.ellipse(medal_bbox, fill='gold')

    # --- Draw the star on the medal ---
    star_color = (184, 134, 11) # Darker gold color for the star

    star_outer_radius = medal_radius * 0.8
    star_inner_radius = medal_radius * 0.35 # Adjust for desired star pointiness
    star_center_x = medal_center_x
    star_center_y = medal_center_y

    star_points = []
    # Loop to generate points for a 5-pointed star
    for i in range(5):
        # Outer point
        # Start angle adjusted so one point is directly upwards
        angle_outer = math.radians(90 + i * 72) # 72 degrees between points (360/5)
        x_outer = star_center_x + star_outer_radius * math.cos(angle_outer)
        y_outer = star_center_y - star_outer_radius * math.sin(angle_outer) # Subtract for y-axis inversion in graphics

        # Inner point
        angle_inner = math.radians(90 + i * 72 + 36) # Halfway between outer points
        x_inner = star_center_x + star_inner_radius * math.cos(angle_inner)
        y_inner = star_center_y - star_inner_radius * math.sin(angle_inner)

        star_points.append((x_outer, y_outer))
        star_points.append((x_inner, y_inner))

    draw.polygon(star_points, fill=star_color)

    return img

#-----------------------------------------------------
# MAIN
#-----------------------------------------------------
if len(sys.argv) != 2:
    raise Exception('Usage: {} <path_to_image.png>'.format(__file__))

original_image_path = sys.argv[1]
animal = os.path.basename(original_image_path.split('.')[0])

#-----------------------------
# 1 Animal
#-----------------------------
move_image_vertical(original_image_path, f'{animal}1jump1.png', 0, direction='up')
move_image_vertical(original_image_path, f'{animal}1jump2.png', 15, direction='up')
move_image_vertical(original_image_path, f'{animal}1jump3.png', 30, direction='up')

#-----------------------------
# 2 Animals
#-----------------------------
create_copies(original_image_path, f'{animal}2jump1.png', animal_placements=[
        {'scale': 1.0, 'position': (0, 0)},
        {'scale': 0.45, 'position': (200, 200)},
    ]
)
move_image_vertical(f'{animal}2jump1.png', f'{animal}2jump2.png', 15, direction='up')
move_image_vertical(f'{animal}2jump1.png', f'{animal}2jump3.png', 30, direction='up')

#-----------------------------
# 3 Animals
#-----------------------------
create_copies(original_image_path, f'{animal}3jump1.png', animal_placements=[
        {'scale': 1.0, 'position': (0, 0)},
        {'scale': 0.45, 'position': (200, 200)},
        {'scale': 0.45, 'position': (60, 200)},
    ]
)
move_image_vertical(f'{animal}3jump1.png', f'{animal}3jump2.png', 15, direction='up')
move_image_vertical(f'{animal}3jump1.png', f'{animal}3jump3.png', 30, direction='up')

#-----------------------------
# 4 Animals
#-----------------------------
create_copies(original_image_path, f'{animal}4jump1.png', animal_placements=[
        {'scale': 1.0, 'position': (0, 0)},
        {'scale': 0.45, 'position': (200, 200)},
        {'scale': 0.45, 'position': (60, 200)},
        {'scale': 0.45, 'position': (130, 220)},
    ]
)
move_image_vertical(f'{animal}4jump1.png', f'{animal}4jump2.png', 15, direction='up')
move_image_vertical(f'{animal}4jump1.png', f'{animal}4jump3.png', 30, direction='up')

#-----------------------------
# Add a medal to the animal
#-----------------------------
medal_image = create_medal_image(400, 400)
medal_image.save("medal.png")
place_medal_on_animal(original_image_path, 'medal.png', f'{animal}_goal.png',
    medal_size=(350, 270),  # Adjust size as needed
    position=(21, 198)) # Adjust x and y for precise placement on the chest

#-----------------------------
# Add animal icon
#-----------------------------
convert_transparent_to_white(original_image_path, f'{animal}_white.png')
create_circular_animal_icon(f'{animal}_white.png', f'{animal}_icon.png')

#-----------------------------
# Create animation XML
#-----------------------------
for i in range(4):
    count = i + 1
    xml = f'''<?xml version="1.0" encoding="utf-8"?>

<animation-list xmlns:android="http://schemas.android.com/apk/res/android" android:oneshot="false">
    <item android:drawable="@drawable/{animal}{count}jump1" android:duration="180"/>
    <item android:drawable="@drawable/{animal}{count}jump2" android:duration="180"/>
    <item android:drawable="@drawable/{animal}{count}jump3" android:duration="180"/>
    <item android:drawable="@drawable/{animal}{count}jump2" android:duration="180"/>
    <item android:drawable="@drawable/{animal}{count}jump1" android:duration="360"/>

</animation-list>'''
    with open(f'{animal}_animation{count}.xml', 'w') as f:
        f.write(xml)

print(f'''
        addAvatar("{animal.capitalize()}", 10, new Integer[]{{
                R.drawable.{animal}1jump1,
                R.drawable.{animal}_animation1,
                R.drawable.{animal}_animation2,
                R.drawable.{animal}_animation3,
                R.drawable.{animal}_animation4,
                R.drawable.{animal}_goal,
                R.drawable.{animal}_icon
        }});
''')