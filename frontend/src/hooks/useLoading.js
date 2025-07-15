import { useState, useCallback } from 'react';

/**
 * Custom hook for managing loading states with error handling
 *
 * @param {Function} defaultFunction - Optional function to execute immediately
 * @returns {Object} Loading state utilities
 */
export function useLoading(defaultFunction) {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    /**
     * Execute an async function with loading state management
     * @param {Function} asyncFunction - The async function to execute
     * @param {any} args - Arguments to pass to the function
     * @returns {Promise<any>} - The result of the async function
     */
    const execute = useCallback(async (asyncFunction, ...args) => {
        setIsLoading(true);
        setError(null);

        try {
            const result = await asyncFunction(...args);
            return result;
        } catch (err) {
            setError(err.message || 'An error occurred');
            throw err;
        } finally {
            setIsLoading(false);
        }
    }, []);

    // Execute the default function if provided
    useState(() => {
        if (defaultFunction) {
            execute(defaultFunction);
        }
    }, [defaultFunction, execute]);

    return {
        isLoading,
        error,
        execute,
        setError, // Allow manually setting errors
        clearError: () => setError(null)
    };
}

// Usage example:
// const { isLoading, error, execute } = useLoading();
// const handleSubmit = () => execute(submitFormData, formData);