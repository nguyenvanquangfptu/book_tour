import React, { createContext, useContext, useState, useEffect } from 'react';

export interface CartItem {
  tourId: number;
  tourTitle: string;
  price: number;
  imageUrl: string;
  guests: number;
  startDate: string;
}

interface CartContextType {
  cart: CartItem[];
  addToCart: (item: CartItem) => void;
  removeFromCart: (tourId: number, startDate: string) => void;
  clearCart: () => void;
  getCartCount: () => number;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const getCartKey = () => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        return 'cart_' + (user.id || user.username || 'guest');
      } catch (e) {}
    }
    return 'cart_guest';
  };

  const [cart, setCart] = useState<CartItem[]>(() => {
    const savedCart = localStorage.getItem(getCartKey());
    return savedCart ? JSON.parse(savedCart) : [];
  });

  useEffect(() => {
    localStorage.setItem(getCartKey(), JSON.stringify(cart));
  }, [cart]);

  const addToCart = (newItem: CartItem) => {
    setCart(prevCart => {
      // Check if same tour and same date exists, then update guests
      const existingItemIndex = prevCart.findIndex(
        item => item.tourId === newItem.tourId && item.startDate === newItem.startDate
      );

      if (existingItemIndex >= 0) {
        const updatedCart = [...prevCart];
        updatedCart[existingItemIndex].guests += newItem.guests;
        return updatedCart;
      }

      return [...prevCart, newItem];
    });
  };

  const removeFromCart = (tourId: number, startDate: string) => {
    setCart(prevCart => prevCart.filter(item => !(item.tourId === tourId && item.startDate === startDate)));
  };

  const clearCart = () => {
    setCart([]);
  };

  const getCartCount = () => {
    return cart.length;
  };

  return (
    <CartContext.Provider value={{ cart, addToCart, removeFromCart, clearCart, getCartCount }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};
