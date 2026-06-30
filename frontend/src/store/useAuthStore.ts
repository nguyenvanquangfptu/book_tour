import { create } from 'zustand';

interface User {
  id: number;
  fullName: string;
  email: string;
  phoneNumber?: string;
  role: string;
  avatarUrl?: string;
  avatar?: string;
  username?: string;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (userData: any, token: string) => void;
  logout: () => void;
  updateUser: (userData: Partial<User>) => void;
}

// Retrieve initial state from localStorage if available
const getInitialUser = (): User | null => {
  try {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  } catch {
    return null;
  }
};

export const useAuthStore = create<AuthState>((set) => ({
  user: getInitialUser(),
  isAuthenticated: !!localStorage.getItem('token'),
  
  login: (userData, token) => {
    localStorage.setItem('user', JSON.stringify(userData));
    localStorage.setItem('token', token);
    set({ user: userData, isAuthenticated: true });
    // Fetch cart data after successful login
    import('./useCartStore').then(({ useCartStore }) => {
      useCartStore.getState().fetchCart();
    });
  },
  
  logout: () => {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    set({ user: null, isAuthenticated: false });
    // Clear cart data from frontend on logout
    import('./useCartStore').then(({ useCartStore }) => {
      useCartStore.setState({ cart: [] });
    });
  },

  updateUser: (userData) => {
    set((state) => {
      if (!state.user) return state;
      const updatedUser = { ...state.user, ...userData };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      return { user: updatedUser };
    });
  }
}));
