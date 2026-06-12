const axios = require('axios');

const api = axios.create({
  baseURL: 'http://localhost:8081/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error)
);

async function test() {
  try {
    const response = await api.get('/tours/search?page=0&size=6');
    console.log("Raw response from interceptor:");
    console.log(response);
    
    const returnedData = response.data?.data || response.data;
    console.log("Returned data by TourService:");
    console.log(returnedData);
    
    if (returnedData && returnedData.content && returnedData.content.length > 0) {
      console.log("SUCCESS! Tours array length:", returnedData.content.length);
    } else {
      console.log("FAILED! data.content is empty or invalid");
    }
  } catch (error) {
    console.error("Error:", error.message);
  }
}

test();
