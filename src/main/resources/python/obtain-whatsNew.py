import sys
import json
import snscrape.modules.twitter as sntwitter
from datetime import datetime, timedelta

keyword = sys.argv[1]
since_date = (datetime.now() - timedelta(days=7)).strftime('%Y-%m-%d')
query = f'{keyword} since:{since_date}'

results = []
for i, tweet in enumerate(sntwitter.TwitterSearchScraper(query).get_items()):
    if i > 20:
        break
    results.append({
        "date": tweet.date.isoformat(),
        "content": tweet.content
    })

print(json.dumps(results))