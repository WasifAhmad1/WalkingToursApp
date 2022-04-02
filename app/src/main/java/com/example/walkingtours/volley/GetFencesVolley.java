package com.example.walkingtours.volley;
//in this class we will use a volley request to get the data back from the api at http://www.christopherhield.com/data/WalkingTourContent.json

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.example.walkingtours.Fence;
import com.example.walkingtours.FenceManager;
import com.example.walkingtours.MapsActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GetFencesVolley {
    public static final String urlString = "http://www.christopherhield.com/data/WalkingTourContent.json";
    public static ArrayList<Fence> fences = new ArrayList<Fence>();
    public static ArrayList<LatLng> paths = new ArrayList<LatLng>();

    public static void getFences(MapsActivity mapsActivity) {
        RequestQueue queue = Volley.newRequestQueue(mapsActivity);

        //ArrayList<Fence> fences = new ArrayList<Fence>();
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("fences");
                    for (int i = 0 ; i<jsonArray.length(); i++){
                        JSONObject json = jsonArray.getJSONObject(i);
                        String id = json.getString("id");
                        String address = json.getString("address");
                        String latitude = json.getString("latitude");
                        String longitude = json.getString("longitude");
                        String radius = json.getString("radius");
                        String description = json.getString("description");
                        String color = json.getString("fenceColor");
                        String image = json.getString("image");
                        Fence fence = new Fence(id, address, Double.parseDouble(latitude), Double.parseDouble(longitude),
                                Float.parseFloat(radius), description, color, image);
                        fences.add(fence);
                    }
                    JSONArray getPath = response.getJSONArray("path");
                    for(int i = 0 ; i<getPath.length(); i++){
                        String latLng = getPath.getString(i);
                        String [] split = latLng.split(",");
                        double lng = Double.parseDouble(split[0]);
                        double lat = Double.parseDouble(split[1]);
                        LatLng latLngValue = new LatLng(lat, lng);
                        paths.add(latLngValue);


                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mapsActivity.genList(fences);
                mapsActivity.genPolyLine(paths);


            }
        };

        Response.ErrorListener error = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };

        JsonRequest<JSONObject> jsonRequest = new JsonRequest<JSONObject>(
                Request.Method.GET, urlString, null, listener, error) {

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // This method is always the same!
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        queue.add(jsonRequest);

        //return fences;

    }

}
