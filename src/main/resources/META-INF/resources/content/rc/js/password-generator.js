var PasswordGenerator = {};
PasswordGenerator.genPassword = function(plength) {
	var keylistalpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	var keylistint = "123456789";
	var keylistspec = "!@#_%$";
	var temp = '';
	var len = plength / 2;
	var len = len - 1;
	var lenspec = plength - len - len;

	for (i = 0; i < len; i++)
		temp += keylistalpha.charAt(Math.floor(Math.random() * keylistalpha.length));

	for (i = 0; i < lenspec; i++)
		temp += keylistspec.charAt(Math.floor(Math.random() * keylistspec.length));

	for (i = 0; i < len; i++)
		temp += keylistint.charAt(Math.floor(Math.random() * keylistint.length));

	temp = temp.split('').sort(function() {
		return 0.5 - Math.random()
	}).join('');

	return temp;
}

jQuery(document).ready(function() {
	jQuery("#readKeyGenerator").click(function() {
		jQuery("#readKey").val(PasswordGenerator.genPassword(8));
	});
	jQuery("#writeKeyGenerator").click(function() {
		jQuery("#writeKey").val(PasswordGenerator.genPassword(8));
	});
});