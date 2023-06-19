We, at NRI, are thrilled to present our submission for this hackathon - a cutting-edge video conferencing application that breaks language barriers by providing live voice translation in the user's voice!

🌟 Inspiration
In an increasingly globalized world, communication across borders is essential. Yet, language barriers often hinder seamless interaction. Our team was inspired to bridge this gap and create an application that enables users to communicate effortlessly, irrespective of the language they speak.

🛠️ How We Built It
We chose SpringBoot as our back-end framework due to its robustness and scalability. For handling real-time communication, we integrated Kurento Media Server. This enabled us to capture audio and video streams efficiently.

To achieve voice translation, we built neural networks for speech recognition and machine translation. For our LLM we utilized both ChatGPT and other models for errors in our speech transcription and to assist us with translation. We also employed speech synthesis models to replicate the user's voice in different languages.

🚀 Live Demo
Experience our application in action! Visit our live demo and join the room with the code "nri5764a7cc". Please note, we’ve limited the demo to this specific room to keep costs minimal.

For enthusiasts interested in running the application locally, please contact us for the external libraries as we have not made our models/interface public.

🧩 What We Learned
This project was a treasure trove of learning experiences. We dived into the depths of speech recognition, machine translation, and real-time communication. We experimented with neural networks, optimized audio stream handling, and explored the challenges that come with latency issues.

🚧 Challenges and Future Directions
One of the significant challenges we faced was latency between receiving the audio from the user and returning the translated voice. We are actively working on optimizing this by capturing the audio directly through JavaScript during voice activity instead of at fixed intervals. This adjustment will vastly reduce latency and improve user experience.

We foresee implementing this optimization and continuously refining the model we had created, but were unable to implement in the allotted timeframe. Our journey doesn’t end here, and we have begun isolating our system from any external dependencies and expanding our databricks dataset.

💡 In Conclusion
NRI's video conferencing application represents a step toward a world where language is no longer a barrier to communication. We believe in the power of connection, and through our innovation, we strive to bring people closer, no matter where they are or what language they speak.

We are excited to hear your feedback and are eager to continue improving our application.

Thank you for your time and consideration.