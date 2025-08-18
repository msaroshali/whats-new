const fs = require('fs');
const args = process.argv.slice(2); // keyword, username
const keyword = args[0] || "";
const username = args[1] || "";
const baseUrl = "https://x.com/i/api/graphql/4fpceYZ6-YQCx_JSl_Cn_A/SearchTimeline";

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

//console.error(`Making request to: ${baseUrl}`);


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
    const output_file = "tweet_response.json";
    let tweets = [];

    // Write the JSON data to a file
    fs.writeFileSync(output_file, JSON.stringify(data, null, 2), 'utf-8');
    

    try {
        const instructions = data.data.search_by_raw_query.search_timeline.timeline.instructions;
        const entries = instructions[0].entries;

        for (let i = 0; i < entries.length && tweets.length < 20; i++) {
            const entry = entries[i];
            try {
                const legacy = entry.content.itemContent.tweet_results.result.legacy;
                tweets.push({
                    date: legacy.created_at,
                    content: legacy.full_text
                });
            } catch (err) {
                // skip if structure not found
            }
        }
    } catch (err) {
        console.error("Error extracting tweets:", err);
    }

        // return only the cleaned array to stdout to be picked up by Java
        console.log(JSON.stringify(tweets, null, 2));

    console.error(`Successfully wrote the response to ${output_file}`);
})
.catch(e => {
    // Handle any errors
    console.error('An error occurred:', e);
});