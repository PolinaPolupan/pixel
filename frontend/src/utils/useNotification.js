import { useState, useEffect } from 'react';

export function useNotification(timeout = 5000) {
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => setError(null), timeout);
      return () => clearTimeout(timer);
    }
  }, [error, timeout]);

  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(null), timeout);
      return () => clearTimeout(timer);
    }
  }, [success, timeout]);

  return {
    error,
    success,
    setError,
    setSuccess,
    clearError: () => setError(null),
    clearSuccess: () => setSuccess(null)
  };
}