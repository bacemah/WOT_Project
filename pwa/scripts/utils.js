export function switchToSubdomain(subdomain,path){
    return `${window.location.protocol}//${window.location.host.replace(/^([^.])*/,subdomain)}/${path}`;
}