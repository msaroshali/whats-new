import { writeFileSync } from 'fs';
import 'dotenv/config.js'; // Load environment variables from .env file
const args = process.argv.slice(2); // keyword, username
const keyword = args[0] || "";
const username = args[1] || "";
const AUTH_TOKEN = args[2] || "";
const X_COOKIE = args[3] || "";
const X_CSRF_TOKEN = args[4] || "";
const baseUrl = "https://x.com/i/api/graphql/4fpceYZ6-YQCx_JSl_Cn_A/SearchTimeline";

const variables = {
    rawQuery: `from:${username} ${keyword}`,
    "count": 20,
    "querySource": "typed_query",
    "product": "Latest",
    "withGrokTranslatedBio": false
};

const headers = {
    "accept": "*/*",
    "accept-encoding": "gzip, deflate, br, zstd",
    "accept-language": "en,en-US;q=0.9",
    "authorization": AUTH_TOKEN,
    "content-type": "application/json",
    "cookie": X_COOKIE,
    "dnt": "1",
    "priority": "u=1, i",
    "referer": "https://x.com/search?q=from%3Aclashreport%20india&src=typed_query&f=live",
    "sec-ch-ua": "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"Google Chrome\";v=\"138\"",
    "sec-ch-ua-mobile": "?0",
    "sec-ch-ua-platform": "\"Windows\"",
    "sec-fetch-dest": "empty",
    "sec-fetch-mode": "cors",
    "sec-fetch-site": "same-origin",
    "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36",
    "x-client-transaction-id": "YTTr4IFSASPPsMvChnACvQtgcM0fuM8TWk56ggKPJV+i8u/nRU10IrKbaEHzzMuMuHXfMGUy3NDPqayBUora52hECNLZYg",
    "x-csrf-token":X_CSRF_TOKEN,
    "x-twitter-active-user": "yes",
    "x-twitter-auth-type": "OAuth2Session",
    "x-twitter-client-language": "en",
    "x-xp-forwarded-for": "02577dfb1337f7f1df9972f96b659f8734c8d9c1a78fee02e422b4a41ff62315094855098bf9f9158e7aafbb791c880986d4700eac00dce1eb949844a848d2c3f2bf4a9c856e89d2d08135af26777dbb0729a2a2f8ab19dcf4944488cdb232eeeb49f43033506ebf8892bcf9db57c33271b60c1dd6a218b0122c855553cc7380491406a24d7c14d0ed2b4c541334e68979548aa7b52248b2959ad90ce65122675c58b65f96205d69baa26762db0ff1dd32a05ece86c34d172d685a786b9a4120d2b1709710549688bd19697a4894df2ad6379c66b7552d2c85127296ebfa6ab52685545e89255d57ae2b3932616fbf13b7996de9e782d09c839221"
  };

const features = {
  "rweb_video_screen_enabled": false,
  "payments_enabled": false,
  "rweb_xchat_enabled": false,
  "profile_label_improvements_pcf_label_in_post_enabled": true,
  "rweb_tipjar_consumption_enabled": true,
  "verified_phone_label_enabled": false,
  "creator_subscriptions_tweet_preview_api_enabled": true,
  "responsive_web_graphql_timeline_navigation_enabled": true,
  "responsive_web_graphql_skip_user_profile_image_extensions_enabled": false,
  "premium_content_api_read_enabled": false,
  "communities_web_enable_tweet_community_results_fetch": true,
  "c9s_tweet_anatomy_moderator_badge_enabled": true,
  "responsive_web_grok_analyze_button_fetch_trends_enabled": false,
  "responsive_web_grok_analyze_post_followups_enabled": true,
  "responsive_web_jetfuel_frame": true,
  "responsive_web_grok_share_attachment_enabled": true,
  "articles_preview_enabled": true,
  "responsive_web_edit_tweet_api_enabled": true,
  "graphql_is_translatable_rweb_tweet_is_translatable_enabled": true,
  "view_counts_everywhere_api_enabled": true,
  "longform_notetweets_consumption_enabled": true,
  "responsive_web_twitter_article_tweet_consumption_enabled": true,
  "tweet_awards_web_tipping_enabled": false,
  "responsive_web_grok_show_grok_translated_post": false,
  "responsive_web_grok_analysis_button_from_backend": true,
  "creator_subscriptions_quote_tweet_preview_enabled": false,
  "freedom_of_speech_not_reach_fetch_enabled": true,
  "standardized_nudges_misinfo": true,
  "tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled": true,
  "longform_notetweets_rich_text_read_enabled": true,
  "longform_notetweets_inline_media_enabled": true,
  "responsive_web_grok_image_annotation_enabled": true,
  "responsive_web_grok_imagine_annotation_enabled": true,
  "responsive_web_grok_community_note_auto_translation_is_enabled": false,
  "responsive_web_enhance_cards_enabled": false

};
// console.log(process.env.X_AUTH_TOKEN)

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
    writeFileSync(output_file, JSON.stringify(data, null, 2), 'utf-8');
    

    try {
        
        const instructions = data.data.search_by_raw_query.search_timeline.timeline.instructions;
        const entries = instructions[0].entries;

        for (let i = 0; i < entries.length && tweets.length < 20; i++) {
            const entry = entries[i];
            try {
                const legacy = entry.content.itemContent.tweet_results.result.legacy;
                const srcUrlId = entry.content.itemContent.tweet_results.result.rest_id;
                
                if (username == "") {
                   const usernameStr = entry.content.itemContent.tweet_results.result.core.user_results.result.core.screen_name;
                   let url = `https://x.com/${usernameStr}/status/${srcUrlId}`;

                   tweets.push({
                    date: legacy.created_at,
                    content: legacy.full_text,
                    sourceUrl: url
                });
                }
                else{

                    let url = `https://x.com/${username}/status/${srcUrlId}`;

                    tweets.push({
                        date: legacy.created_at,
                        content: legacy.full_text,
                        sourceUrl: url
                    });
                }                
            } catch (err) {
                // skip if structure not found
            }
        }
    } catch (err) {
        console.error("Error extracting tweets:", err);
    }

        // return only the cleaned array to stdout to be picked up by Java
        console.log(JSON.stringify(tweets, null, 3));

    console.error(`Successfully wrote the response to ${output_file}`);
})
.catch(e => {
    // Handle any errors
    console.error('An error occurred:', e);
});