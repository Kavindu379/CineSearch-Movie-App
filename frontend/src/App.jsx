import { useState } from 'react'
import './App.css'

function App() {
  const [searchString, setSearchString] = useState('')
  const [movies, setMovies] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleSearch = async (e) => {
    e.preventDefault()
    if (!searchString.trim()) return

    setLoading(true)
    setError(null)
    
    try {
      const response = await fetch('http://localhost:8080/api/movies/search', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ searchString }),
      })

      if (!response.ok) {
        throw new Error('Failed to connect to the movie service.')
      }

      const data = await response.json()
      setMovies(data)
    } catch (err) {
      console.error('Search error:', err)
      setError('The movie service is currently offline. Please try again later.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="app-container">
      <div className="search-section">
        <h1 className="title">CineSearch</h1>
        <p className="subtitle">Discover your next cinematic masterpiece</p>
        
        <form className="search-box" onSubmit={handleSearch}>
          <div className="input-wrapper">
            <svg className="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
            <input
              type="text"
              className="search-input"
              placeholder="Describe what you're in the mood for..."
              value={searchString}
              onChange={(e) => setSearchString(e.target.value)}
            />
          </div>
          <button type="submit" className="search-button" disabled={loading}>
            {loading ? (
              <span className="loader"></span>
            ) : (
              'Search'
            )}
          </button>
        </form>

        {error && <div className="error-banner">{error}</div>}
      </div>

      <div className={`results-container ${movies.length > 0 ? 'visible' : ''}`}>
        <div className="results-grid">
          {movies.map((movie, index) => (
            <div key={index} className="movie-card" style={{ animationDelay: `${index * 0.1}s` }}>
              <div className="card-image">
                {!movie.poster_path ? (
                   <div className="card-image-placeholder">
                      <span className="play-icon">▶</span>
                   </div>
                ) : (
                  <img src={movie.poster_path} alt={movie.title} />
                )}
                <div className="overlay">
                   <span className="play-icon-overlay">▶</span>
                </div>
              </div>
              <div className="card-content">
                <span className="tag">{movie.vote_average > 7 ? 'Must Watch' : 'Popular'}</span>
                <h3>{movie.title}</h3>
                <div className="card-footer">
                  <span>{movie.release_date?.split('-')[0] || 'N/A'}</span>
                  <span className="rating">★ {movie.vote_average.toFixed(1)}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export default App
