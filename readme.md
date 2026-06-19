# Whats New: AI-Powered News Aggregator & Analyzer

WhatsNew is a modern, AI-powered focused news aggregator built with an extremely lightweight Java backend (Javalin) and a beautiful, responsive frontend. It obtains breaking news from X/Twitter etc. stores them in a local SQLite database, and after applying some RAG magic with cosine similarity, leveraging the **Google Gemini API**, provides advanced semantic(chatbot like) search, automated summaries, and a live news ticker.

##  Features

- **Semantic AI Search**:  Ask natural language questions (e.g., "What happened in the tech industry today?") Don't just search by keywords. The app uses Gemini vector embeddings to find the most contextually relevant news in the database and synthesize an answer.
- **Timeframe Summaries**: Quickly catch up on the news with AI-generated summaries of the past Hour, Day, or Week.
- **Live News Ticker**: A sleek, animated ticker continuously displays the latest breaking news from every distinct news source in the database, randomized for maximum coverage.
- **Resilient AI Backend**: Built-in API Key and Model rotation logic. If the free-tier Gemini API hits a rate limit (429) or a model gets overloaded (503), the backend seamlessly signals the frontend to auto-retry while rotating keys and models to ensure uninterrupted service.
- **Modern UI/UX**: Features glassmorphism, glowing accents, a responsive design, and a fully functional Dark Mode toggle.
- **Secret Themes**: Try to find the hidden heart icon to toggle the secret "Girly Theme"!
- **Background Jobs**: Automated background services routinely fetch new tweets and run database cleanups (fixing corrupted or dummy embeddings).

##  Technology Stack

**Backend**:
- **Java 17+** cz Java is way faster than Python in my usecase scenario.
- **Javalin**: Lightweight web framework for REST endpoints and serving static files.
- **SQLite & JDBC**: Local, file-based database (`news.db`) for storing tweets and vector embeddings.
- **Google Gemini API**: Used for generating text embeddings (`gemini-embedding-2`) and text generation/summarization.
- **SLF4J**: For robust backend logging.

**Frontend**:
- **HTML5 / CSS3 / JavaScript (Vanilla)**
- **TailwindCSS**: For rapid utility-class styling.
- **DataTables & jQuery**: For displaying raw search results.
- **FontAwesome**: For beautiful UI icons.

##  Setup & Installation

### Prerequisites
1. Java Development Kit (JDK 17 or higher)
2. Maven or Gradle (depending on your IDE setup)
3. Multiple Google Gemini API keys (for rate limit rotation)

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/whats-new.git
cd whats-new
```

### 2. Configure API Keys
The application uses an environment variables file to rotate API keys. Create a `.env` file in the root directory and add your keys:
```env
GEMINI_API_KEY=your_primary_key_here
GEMINI_API_KEY1=your_secondary_key_here
GEMINI_API_KEY2=your_tertiary_key_here
```

### 3. Build and Run
Compile and run the `Find.java` main class. This will:
- Initialize the SQLite database (`news.db`) if it doesn't exist.
- Start the background tasks for fetching tweets and repairing embeddings.
- Launch the Javalin web server on port `7070`.

### 4. Access the Application
Open your browser and navigate to:
```
http://localhost:7070
```

##  Project Structure

- `src/main/java/aboutNew/Find.java`: The main entry point. Sets up routes and background jobs.
- `src/main/java/aboutNew/controller/TweetController.java`: Handles all API endpoints (`/search`, `/latest`, `/ai-search`, `/summary`, `/ticker`).
- `src/main/java/aboutNew/dao/TweetDAO.java`: Manages SQLite database interactions, vector embedding storage, and background cleanups.
- `src/main/java/aboutNew/GeminiService.java`: Interfaces with Gemini for text generation.
- `src/main/java/aboutNew/EmbeddingService.java`: Interfaces with Gemini for vector embeddings.
- `src/main/java/aboutNew/Rotator.java`: Handles rotating API keys and Models on limits/failures.
- `src/main/resources/public/`: Contains all frontend assets (`index.html`, `script.js`, `styleAI.css`).

## 🤝 Contributing
Contributions, issues, and feature requests are welcome! Feel free to check the issues page. Get in touch if it does not work for you.

## 📝 License
This project is licensed under the MIT License.
