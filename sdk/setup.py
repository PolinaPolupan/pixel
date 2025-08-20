from setuptools import setup, find_packages

setup(
    name="pixel-sdk",
    version="1.0.0",
    author="pixel",
    author_email="polina@example.com",
    description="Python client library for the Scene Service API",
    long_description_content_type="text/markdown",
    url="https://github.com/PolinaPolupan/scene-service-sdk",
    packages=find_packages(),
    include_package_data=True,
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires=">=3.6",
    install_requires=[
        "requests>=2.25.0",
    ],
)