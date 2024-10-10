Diary Video Feature Extraction Framework

Overview

This repository contains a Python-based framework designed to extract various features from diary video recordings, including linguistic, symptom, facial, and speech features. These features are stored in JSON format for further analysis or integration with other systems, potentially using FHIR standards for healthcare data exchange.

Key Features

Multimodal Feature Extraction: Extracts linguistic, symptom, facial, and speech features from diary videos.
Quality Check: Ensures video quality meets specific criteria before proceeding with feature extraction.
Video Splitting: Divides videos into 30-second segments for efficient analysis.
Feature Storage: Stores extracted features in JSON format for easy access and analysis.
FHIR Integration: Potential for integrating with FHIR-compatible systems for healthcare data exchange.
Prerequisites

Python 3.x
Required libraries:
Stanza, Spacy, NLTK (for linguistic features)
MedicalNER (for symptom extraction)
OpenFace (for facial features)
Parselmouth, myprosody (for speech features)
FFmpeg (for video splitting)
FHIR libraries (if integrating with FHIR)

Installation

1. Clone this repository
2. Install required libraries:
3. Usage

   3.1. Prepare your diary videos.

   3.2. Run the main script (flask_smile_v8.py).

   3.3. The extracted features will be stored in the specified output directory.
 
Contributing

Contributions are welcome! Please feel free to fork the repository and submit pull requests.

License

This project is licensed under the CC-BY License.   

Additional Notes

For more detailed usage instructions and customization options, refer to the documentation within the repository.
Consider adding more specific information about the project's purpose, target audience, and potential applications.
If you have any questions or require further assistance, please don't hesitate to contact [email]

Reference
MLAKAR, Izidor, ARIOZ, Umut, SMRKE, Urška, PLOHL, Nejc, ŠAFRAN, Valentino, ROJC, Matej. An end-to-end framework for extracting observable cues of depression from diary recordings. Expert systems with applications. [Online ed.]. Available online 8 August 2024, 125025, 41 str., ilustr. ISSN 1873-6793. DOI: 10.1016/j.eswa.2024.125025.
