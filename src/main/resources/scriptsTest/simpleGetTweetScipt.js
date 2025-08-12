const fs = require('fs');

const baseUrl = "https://x.com/i/api/graphql/S5foUwTDpmfMUKFqoQK_1g/UserTweets";

const variables = {

};

const headers = {
    };

const features = {
    };

const fieldToggles = {
    "withArticlePlainText": false
};

// Construct the URL with query parameters
// Use URLSearchParams to handle the query parameters
const params = new URLSearchParams({
    variables: JSON.stringify(variables),
    features: JSON.stringify(features),
    fieldToggles: JSON.stringify(fieldToggles)
});

// Construct the full URL
const fullUrl = `${baseUrl}?${params.toString()}`;

console.log(`Making request to: ${baseUrl}`);


fetch(fullUrl, {
    method: 'GET',
    headers: headers
})
.then(response => {
    // Check if the request was successful
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    // Parse the response as JSON
    return response.json();
})
.then(data => {
    const output_file = "tweets_response.json";
    
    // Write the JSON data to a file
    fs.writeFileSync(output_file, JSON.stringify(data, null, 2), 'utf-8');
    
    console.log(`Successfully wrote the response to ${output_file}`);
})
.catch(e => {
    // Handle any errors
    console.error('An error occurred:', e);
});