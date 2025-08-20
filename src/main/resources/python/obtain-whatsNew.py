import sys
import json
import tweepy


keyword = sys.argv[1]
account = sys.argv[2]
twitterToken = sys.argv[3]
 
# 1. Twitter API credentials
BEARER_TOKEN = twitterToken

# 2. Create Twitter API client
client = tweepy.Client(bearer_token=BEARER_TOKEN)

# 3. Get keyword from Java arguments
if len(sys.argv) < 3:
    print("[]")
    sys.exit(0)


# # Build query for a specific account
query = f"{keyword} from:{account}"

# 4. Search recent tweets 

tweets_data = []
try:
    tweets = client.search_recent_tweets(
        query=query,
        tweet_fields=["created_at", "text", "author_id", "lang"],
        max_results=10
    )

    for tweet in tweets.data or []:
        tweets_data.append({
            "date": str(tweet.created_at),
            "content": tweet.text
        })

except Exception as e:
    tweets_data.append({"error": str(e)})

# 5. Print the results as JSON
print(json.dumps(tweets_data))