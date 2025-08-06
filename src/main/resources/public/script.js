function search() {
  const keyword = document.getElementById("keyword").value;
  const username = document.getElementById("username").value;

  fetch(`http://localhost:7070/search?keyword=${encodeURIComponent(keyword)}&username=${encodeURIComponent(username)}`)
      .then(response => response.json())
      .then(data => {
          const results = document.getElementById("results");
          results.innerHTML = "";

          if (!Array.isArray(data)) {
              results.innerHTML = `<p style="color:red;">Unexpected response: ${JSON.stringify(data)}</p>`;
              return;
          }

          if (data.length === 0) {
              results.innerHTML = "<p>No tweets found.</p>";
              return;
          }

          data.forEach(tweet => {
              if (tweet.error) {
                  results.innerHTML += `<p style="color:red;">Error: ${tweet.error}</p>`;
              } else {
                  results.innerHTML += `<p><strong>${tweet.date}</strong>: ${tweet.content}</p>`;
              }
          });
      })
      .catch(err => {
          document.getElementById("results").innerHTML = `<p style="color:red;">Fetch error: ${err}</p>`;
      });
}
