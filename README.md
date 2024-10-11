Diary Video Feature Extraction Framework

![framework pipeline](https://github.com/HUMADEX/FeatureExtraction-Cloud-Pipline/blob/main/docs/Feature_Extraction_Pipeline.jpg)

Overview

This repository contains a Python-based framework designed to extract various features from diary video recordings, including linguistic, symptom, facial, and speech features. These features are stored in JSON format for further analysis or integration with other systems, potentially using FHIR standards for healthcare data exchange.

Additional to the Python-based framework repository contains the Spring Boot app to provide the entry point to the framwork for integration and testing purposes. Spring Boot app provides Swagger (OpenAPI) UI for the HTTP endpoints that are defined in the SpringBootRouter Java class with the Camel REST DSL. Recieved HTTP requests can be routed to the Service classes and handled forward as needed.

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

Apache Maven 3.9.6
Java JDK 17.0.12

Please note that AI microservices that are connected in the code like Automatic Speech Recognition (ASR), Named Entity Recognition (NER) and FHIR server (HAPI FHIR) are configured externally and connected over the outside REST endpoints which should be approprietly configured in the code parts where needed.

Installation

1. Clone this repository
2. Install required libraries:
3. Usage

   3.1. Prepare your diary videos.

   3.2. Run the main script (flask_smile_v8.py).
   
   3.3. Check the Swagger UI in the web broswer: http://your-server-ip:8080.
   
   3.4. Select video and start processing.

   3.5. The extracted features will be stored in the specified output directory.

   3.6. All extracted results are on the end stored on a FHIR server as Composition resources.
 
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

Acknowledgment

This work has been funded by the European Union Horizon Europe Research and Innovation Programme projects SMILE (grant number 101080923) and the Slovenian Research and Innovation Agency, Advanced methods of interaction in telecommunication research programme (grant number P2-0069). The content of this paper does not reflect the official position of the European Union or any other institution. The information and views expressed are the sole responsibility of the authors.
