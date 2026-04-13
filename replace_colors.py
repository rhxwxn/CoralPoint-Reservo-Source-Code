import os

target = "0xFF0F5A73"
replacement = "0xFF054659"
path = r"c:\Users\kristine\AndroidStudioProjects\CoralPointReservo\app\src\main\java\com\example\coralpointreservo"

count = 0
for root, _, files in os.walk(path):
    for f in files:
        if f.endswith('.kt'):
            fpath = os.path.join(root, f)
            with open(fpath, 'r', encoding='utf-8') as file:
                content = file.read()
            if target in content:
                content = content.replace(target, replacement)
                with open(fpath, 'w', encoding='utf-8') as file:
                    file.write(content)
                count += 1
                print(f"Updated {f}")

print(f"Total files updated: {count}")
