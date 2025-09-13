import { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Lenis from 'lenis';
import viteLogo from '/vite.svg';
import './App.css';
import Landing from '../pages/Landing';
import UploadComponent from '../pages/UploadComponent';
import AuthPages from '../pages/AuthPages';
import Navbar from '../components/Navbar';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true); // Add loading state

  // Check authentication status on app load
  useEffect(() => {
    const verifyAuth = async () => {
      const token = localStorage.getItem('authToken');
      const userData = localStorage.getItem('userData');

      if (token && userData) {
        try {
          // Verify token with backend
          const response = await fetch('http://localhost:3001/auth/verify', {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json',
            },
          });

          if (response.ok) {
            setIsAuthenticated(true);
            setUser(JSON.parse(userData));
          } else {
            // Invalid token, clear local storage
            localStorage.removeItem('authToken');
            localStorage.removeItem('userData');
            setIsAuthenticated(false);
            setUser(null);
          }
        } catch (error) {
          console.error('Auth verification failed:', error);
          localStorage.removeItem('authToken');
          localStorage.removeItem('userData');
          setIsAuthenticated(false);
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    verifyAuth();
  }, []);

  // Initialize Lenis for smooth scrolling
  useEffect(() => {
    const lenis = new Lenis({
      duration: 1.2,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
      smooth: true,
    });

    function raf(time) {
      lenis.raf(time);
      requestAnimationFrame(raf);
    }
    requestAnimationFrame(raf);

    return () => {
      lenis.destroy();
    };
  }, []);

  // Handle login
  const handleLogin = (userData, token) => {
    setIsAuthenticated(true);
    setUser(userData);
    localStorage.setItem('authToken', token);
    localStorage.setItem('userData', JSON.stringify(userData));
  };

  // Handle logout
  const handleLogout = async () => {
    try {
      await fetch('http://localhost:3001/auth/logout', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
          'Content-Type': 'application/json',
        },
      });
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setIsAuthenticated(false);
      setUser(null);
      localStorage.removeItem('authToken');
      localStorage.removeItem('userData');
    }
  };

  // Protected Route Component
  const ProtectedRoute = ({ children }) => {
    if (isLoading) {
      return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
    }
    return isAuthenticated ? children : <Navigate to="/auth" replace />;
  };

  // Public Route Component
  const PublicRoute = ({ children }) => {
    if (isLoading) {
      return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
    }
    return !isAuthenticated ? children : <Navigate to="/dashboard" replace />;
  };

  // Basic Error Boundary Component
  const ErrorBoundary = ({ children }) => {
    try {
      return children;
    } catch (error) {
      console.error('Error in component:', error);
      return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center p-4">
            <h2 className="text-xl font-bold text-red-600">Something went wrong</h2>
            <p className="text-gray-600">Please try refreshing the page or contact support.</p>
          </div>
        </div>
      );
    }
  };

  return (
    <Router>
      <ErrorBoundary>
        <div className="App">
          {isAuthenticated && !isLoading && (
            <Navbar user={user} onLogout={handleLogout} />
          )}

          <Routes>
            {/* Public Routes */}
            <Route
              path="/"
              element={
                <PublicRoute>
                  <AuthPages onLogin={handleLogin} />
                </PublicRoute>
              }
            />

            {/* Protected Routes */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <UploadComponent />
                </ProtectedRoute>
              }
            />
            <Route
              path="/upload"
              element={
                <ProtectedRoute>
                  <UploadComponent />
                </ProtectedRoute>
              }
            />

            {/* Catch-all route */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>
      </ErrorBoundary>
    </Router>
  );
}

export default App;