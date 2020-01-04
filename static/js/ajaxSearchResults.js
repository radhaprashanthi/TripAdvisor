function showSearchResults(city){
    var name = 'Hilton'
    var xhttp = new XMLHttpRequest();
    if (city == "" && name == "") {
	    document.getElementById("searchResultsCity").innerHTML = "No city or search word selected ";
	    return;
    }
  
    xhttp.onreadystatechange = function() {
       if (this.readyState == 4 && this.status == 200) {
        document.getElementById("searchResultsCity").innerHTML = this.responseText;
       }
    };
    xhttp.open("GET", "welcome?city=" + city + "&name=" + name, true);
    xhttp.send();
}
