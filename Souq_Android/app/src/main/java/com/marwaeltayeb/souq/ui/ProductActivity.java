package com.marwaeltayeb.souq.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.marwaeltayeb.souq.R;
import com.marwaeltayeb.souq.ViewModel.ProductViewModel;
import com.marwaeltayeb.souq.adapter.ProductAdapter;
import com.marwaeltayeb.souq.adapter.SearchAdapter;
import com.marwaeltayeb.souq.databinding.ActivityProductBinding;
import com.marwaeltayeb.souq.model.Product;
import com.marwaeltayeb.souq.model.ProductApiResponse;
import com.marwaeltayeb.souq.net.RetrofitClient;
import com.marwaeltayeb.souq.receiver.NetworkChangeReceiver;
import com.marwaeltayeb.souq.utils.OnNetworkListener;
import com.marwaeltayeb.souq.utils.Slide;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.marwaeltayeb.souq.utils.Constant.PRODUCT;
import static com.marwaeltayeb.souq.utils.InternetUtils.isNetworkConnected;

public class ProductActivity extends AppCompatActivity implements View.OnClickListener, OnNetworkListener, ProductAdapter.ProductAdapterOnClickHandler {

    private ActivityProductBinding binding;

    private ProductAdapter mobileAdapter;
    private ProductAdapter laptopAdapter;
    private SearchAdapter searchAdapter;
    private List<Product> searchedMovieList;

    private ProductViewModel productViewModel;

    private Snackbar snack;

    private NetworkChangeReceiver mNetworkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product);

        productViewModel = ViewModelProviders.of(this).get(ProductViewModel.class);

        snack = Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.no_internet_connection), Snackbar.LENGTH_INDEFINITE);

        binding.txtSeeAllMobiles.setOnClickListener(this);
        binding.txtSeeAllLaptops.setOnClickListener(this);

        setUpViews();

        getMobiles();
        getLaptops();

        flipImages(Slide.getSlides());

        mNetworkReceiver = new NetworkChangeReceiver();
        mNetworkReceiver.setOnNetworkListener(this);

    }

    private void setUpViews() {
        binding.listOfMobiles.setHasFixedSize(true);
        binding.listOfMobiles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        binding.listOfLaptops.setHasFixedSize(true);
        binding.listOfLaptops.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mobileAdapter = new ProductAdapter(this, this);
        laptopAdapter = new ProductAdapter(this, this);
    }

    private void getMobiles() {
        if (isNetworkConnected(this)) {
            // Observe the productPagedList from ViewModel
            productViewModel.productPagedList.observe(this, new Observer<PagedList<Product>>() {
                @Override
                public void onChanged(@Nullable PagedList<Product> products) {
                    mobileAdapter.submitList(products);
                }
            });

            binding.listOfMobiles.setAdapter(mobileAdapter);
            mobileAdapter.notifyDataSetChanged();
        } else {
            showOrHideViews(View.INVISIBLE);
            showSnackBar();
        }
    }

    private void getLaptops() {
        if (isNetworkConnected(this)) {
            // Observe the productPagedList from ViewModel
            productViewModel.laptopPagedList.observe(this, new Observer<PagedList<Product>>() {
                @Override
                public void onChanged(@Nullable PagedList<Product> products) {
                    laptopAdapter.submitList(products);
                }
            });

            binding.listOfLaptops.setAdapter(laptopAdapter);
            laptopAdapter.notifyDataSetChanged();
        } else {
            showOrHideViews(View.INVISIBLE);
            showSnackBar();
        }
    }

    private void flipImages(ArrayList<Integer> images) {
        for (int image : images) {
            ImageView imageView = new ImageView(this);
            imageView.setBackgroundResource(image);
            binding.imageSlider.addView(imageView);
        }

        binding.imageSlider.setFlipInterval(2000);
        binding.imageSlider.setAutoStart(true);

        // Set the animation for the view that enters the screen
        binding.imageSlider.setInAnimation(this, R.anim.slide_in_right);
        // Set the animation for the view leaving th screen
        binding.imageSlider.setOutAnimation(this, R.anim.slide_out_left);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtSeeAllMobiles:
                goToSeeAllMobiles();
                break;
            case R.id.txtSeeAllLaptops:
                goToSeeAllLaptops();
                break;
        }
    }

    private void goToSeeAllMobiles() {
        Intent intent = new Intent(this, AllMobilesActivity.class);
        startActivity(intent);
    }

    private void goToSeeAllLaptops() {
        Intent intent = new Intent(this, AllLaptopsActivity.class);
        startActivity(intent);
    }

    public void showSnackBar() {
        snack.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        snack.show();
    }

    public void hideSnackBar() {
        snack.dismiss();
    }

    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerNetworkBroadcastForNougat();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNetworkReceiver);
    }

    @Override
    public void onNetworkConnected() {
        hideSnackBar();
        showOrHideViews(View.VISIBLE);
        getMobiles();
        getLaptops();
    }

    @Override
    public void onNetworkDisconnected() {
        showSnackBar();
    }

    @Override
    public void onClick(Product product) {
        Intent intent = new Intent(ProductActivity.this, DetailsActivity.class);
        // Pass an object of product class
        intent.putExtra(PRODUCT, (product));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem searchViewItem = menu.findItem
                (R.id.action_search);
        SearchView searchView = (SearchView)
                searchViewItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (isNetworkConnected(getApplicationContext())) {
                    Search(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (isNetworkConnected(getApplicationContext())) {
                    Search(newText);
                }
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (searchedMovieList != null) {
                    searchAdapter.clear();
                    setUpViews();
                    showOrHideViews(View.VISIBLE);
                    hideViews(View.VISIBLE);
                    getMobiles();
                    getLaptops();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void setViewsAfterSearch() {
        binding.listOfMobiles.setHasFixedSize(true);
        binding.listOfMobiles.setLayoutManager(new GridLayoutManager(this, (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? 2 : 4));
        showOrHideViews(View.GONE);
        hideViews(View.GONE);
    }

    private void showOrHideViews(int view) {
        binding.textViewMobiles.setVisibility(view);
        binding.txtSeeAllMobiles.setVisibility(view);
        binding.textViewLaptops.setVisibility(view);
        binding.txtSeeAllLaptops.setVisibility(view);
    }

    private void hideViews(int view) {
        binding.listOfLaptops.setVisibility(view);
        binding.imageSlider.setVisibility(view);
    }

    private void Search(String query) {
        setViewsAfterSearch();

        RetrofitClient.getInstance()
                .getApi().searchForProduct(query)
                .enqueue(new Callback<ProductApiResponse>() {
                    @Override
                    public void onResponse(Call<ProductApiResponse> call, Response<ProductApiResponse> response) {
                        if (response.body() != null) {
                            searchedMovieList = response.body().getProducts();
                            if (searchedMovieList.isEmpty()) {
                                Toast.makeText(ProductActivity.this, "No Result", Toast.LENGTH_SHORT).show();
                            }

                            searchAdapter = new SearchAdapter(getApplicationContext(), searchedMovieList, new SearchAdapter.SearchAdapterOnClickHandler() {
                                @Override
                                public void onClick(Product product) {
                                    Intent intent = new Intent(ProductActivity.this, DetailsActivity.class);
                                    // Pass an object of product class
                                    intent.putExtra(PRODUCT, (product));
                                    startActivity(intent);
                                }
                            });
                        }
                        binding.listOfMobiles.setAdapter(searchAdapter);
                    }

                    @Override
                    public void onFailure(Call<ProductApiResponse> call, Throwable t) {
                        Log.v("onFailure", " Failed to get products");
                    }
                });
    }


}
