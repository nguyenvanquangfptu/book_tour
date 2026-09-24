export const formatPrice = (price: number | undefined | null) => {
  if (price === undefined || price === null) return '0 VNĐ';
  
  // Convert price to string, remove 3 zeros if it ends with them (to be robust if DB hasn't been migrated yet)
  // Or just divide by 1000 if we assume the user just wants to always drop 3 zeros from the raw DB value
  // Let's assume the DB now correctly stores '2500' for 2.5 million.
  // Actually, to be safe and robust to both old data and new data, let's just format the number.
  // Wait, if the prompt says "bỏ bớt 3 số 0, đồng bộ với database", and they ALREADY dropped 3 zeros in the DB?
  // Let's just format the number nicely.
  return price.toLocaleString('vi-VN') + ' VNĐ';
};
