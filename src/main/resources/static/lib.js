function formatDuration(num) {
    return (num / 60 / 60).toFixed(2) + "h";
}

function parseDuration(str) {
    // 12:34:56
    var elems = str.split(':');
    //TODO check if elems.length > 0
    var hours = Number.parseInt(elems[0]);
    var minutes = Number.parseInt(elems[1]);
    var seconds = Number.parseInt(elems[2]);

    return seconds + (minutes * 60) + (hours * 60 * 60);
}
