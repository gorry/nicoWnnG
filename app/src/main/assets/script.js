
keyboard_12key_pic = new Array(
  "keyboard_12key_toggle.png",
  "keyboard_12key_flick.png",
  "keyboard_12key_2touch.png"
);

keyboard_qwerty_pic = new Array(
  "keyboard_qwerty_roman.png",
  "keyboard_qwerty_roman_compact.png",
  "keyboard_qwerty_roman_mini.png",
  "keyboard_qwerty_roman_mini2.png",
  "keyboard_qwerty_jis.png",
  "keyboard_qwerty_50on.png",
  "keyboard_qwerty_roman_old.png",
  "keyboard_qwerty_50on_old.png",
  "keyboard_qwerty_jis_old.png"
);

function easy_keyboard_landscape_change(obj) {
	var value = obj[obj.selectedIndex].value;
	var type = value.substring(0, 1)-0;
	var id = value.substring(2, 4)-1;
	switch (type) {
	  case 1:
		pic = keyboard_12key_pic[id];
		break;
	  case 2:
		pic = keyboard_qwerty_pic[id];
		break;
	}
	document.easy_keyboard_landscape.src = pic;
}


function easy_keyboard_portrait_change(obj) {
	var value = obj[obj.selectedIndex].value;
	var type = value.substring(0, 1)-0;
	var id = value.substring(2, 4)-1;
	switch (type) {
	  case 1:
		pic = keyboard_12key_pic[id];
		break;
	  case 2:
		pic = keyboard_qwerty_pic[id];
		break;
	}
	document.easy_keyboard_portrait.src = pic;
}

function languageSetting() {
    android.callback("languageSetting");
}

function setting() {
    android.callback("setting");
}

function get_radio_checked(obj) {
	var len = obj.length;
	for (i=0; i<len; i++) {
		if (obj[i].checked) return i;
	}
	return -1;
}

function easy_set() {
	var obj;
	obj = document.easy.easy_keyboard_type_radio;
	var keyboard_type = obj[get_radio_checked(obj)].value;
	obj = document.easy.easy_keyboard_portrait_select;
	var keyboard_portrait = obj[obj.selectedIndex].value;
	obj = document.easy.easy_keyboard_landscape_select;
	var keyboard_landscape = obj[obj.selectedIndex].value;
	obj = document.easy.easy_keyboard_size_radio;
	var keyboard_size = obj[get_radio_checked(obj)].value;
    android.callback("easy,"+keyboard_type+","+keyboard_portrait+","+keyboard_landscape+","+keyboard_size);
}

function easy_get_portrait_keyboard() {
	return android.getPortraitKeyboard();
}

function easy_get_landscape_keyboard() {
	return android.getLandscapeKeyboard();
}

function easy_get_machine_type() {
	return android.getMachineType();
}

function easy_get_keyboard_size() {
	return android.getKeyboardSize();
}

function onload() {
	var i;
	var obj;

	var type = easy_get_machine_type();
	obj = document.easy.easy_keyboard_type_radio;
	for (i=0; i<obj.length; i++) {
		if (obj[i].value == type) {
			obj[i].checked = true;
		} else {
			obj[i].checked = false;
		}
	}

	var portrait = easy_get_portrait_keyboard();
	obj = document.easy.easy_keyboard_portrait_select;
	for (i=0; i<obj.length; i++) {
		if (obj[i].value == portrait) {
			obj.selectedIndex = i;
		}
	}
	easy_keyboard_portrait_change(obj);

	var landscape = easy_get_landscape_keyboard();
	obj = document.easy.easy_keyboard_landscape_select;
	for (i=0; i<obj.length; i++) {
		if (obj[i].value == landscape) {
			obj.selectedIndex = i;
		}
	}
	easy_keyboard_landscape_change(obj);

	var type = easy_get_keyboard_size();
	obj = document.easy.easy_keyboard_size_radio;
	for (i=0; i<obj.length; i++) {
		if (obj[i].value == type) {
			obj[i].checked = true;
		} else {
			obj[i].checked = false;
		}
	}

}

