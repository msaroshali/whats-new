function search() {
    const keyword = document.getElementById("keyword").value;
    fetch("/search?keyword=" + encodeURIComponent(keyword))
      .then(res => res.json())
      .then(data => {
        const results = document.getElementById("results");
        results.innerHTML = "";
        data.forEach(tweet => {
          const p = document.createElement("p");
          p.textContent = `${tweet.date}: ${tweet.content}`;
          results.appendChild(p);
        });
      })
      .catch(err => console.error("Error:", err));
  }
  