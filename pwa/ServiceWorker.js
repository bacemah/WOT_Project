let cacheName = 'PWA-SmartHydro-V1'
let assets = [
'/index.html',
'/pages/signup.html',
'/pages/welcome.html',
'/pages/about.html',
'/pages/dashboard.html',
'/styles/main.css',
'/scripts/main.js',
'/scripts/iam.js' ,
'/images/favicon.svg'

];

caches.keys().then(function(names) {
    for (let name of names)
        caches.delete(name);
});

self.addEventListener("install", installEvent => {
  installEvent.waitUntil(
    caches.open(cacheName).then(cache => {
      cache.addAll(assets);
    })
  );
});

self.addEventListener("fetch", fetchEvent => {
  fetchEvent.respondWith(
    caches.match(fetchEvent.request).then(res => {
      return res || fetch(fetchEvent.request);
    })
  );
});