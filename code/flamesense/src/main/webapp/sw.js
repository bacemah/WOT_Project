let cacheName="pwa01"
let filesToCache = [
    'index.html',
    'styles/main.css',
    'scripts/main.js'
];
/* start the service worker and cache all app's content  */
self.addEventListener('install' , function(e) {
    e.waitUntil(
        caches.open(cacheName).then(function(cache){
            return cache.addAll(filesToCache);
        })
    );
});

/* Serve cached content when offline */

self.addEventListener('fetch' , function(e) {
    e.respondWith(
        caches.match(e.request).then(function(response){
            return response || fetch(e.request) ;
        })
    );

});

