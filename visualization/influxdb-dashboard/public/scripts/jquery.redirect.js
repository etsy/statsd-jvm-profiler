/* jQuery POST/GET redirect method
   v.0.1
	modified by Miguel Galante,https://github.com/mgalante
   v.0.1
   made by Nemanja Avramovic, www.avramovic.info 
   */

;(function( $ ){

	$.redirect = function( target, values, method ) {  

		method = (method && method.toUpperCase() == 'GET') ? 'GET' : 'POST';
			
		if (!values)
		{
			var obj = $.parse_url(target);
			target = obj.url;
			values = obj.params;
		}
					
		var form = $('<form>',{attr:{
			method: method,
			action: target
		}});
		
		for(var i in values)
		{
			$('<input>',{
				attr:{
					type: 'hidden',
					name: i,
					value: values[i]
				}
			}).appendTo(form);

		}
		
		$('body').append(form);
        console.log(form);
		form.submit();
	};
	
	$.parse_url = function(url)
	{
		if (url.indexOf('?') == -1)
			return { url: url, params: {} }
			
		var parts = url.split('?');
		var url = parts[0];
		var query_string = parts[1];
		
		var return_obj = {};
		var elems = query_string.split('&');
		
		var obj = {};
		
		for(var i in elems)
		{
			var elem = elems[i];
			var pair = elem.split('=');
			obj[pair[0]] = pair[1];
		}
		
		return_obj.url = url;
		return_obj.params = obj;
		
		return return_obj;		
	}  	
})( jQuery );