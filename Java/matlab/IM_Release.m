function [z, z_true, var_z, A, vertices, time_elps,count,euc_dist] = IM_Release(true_loc, state_no, eps, DeltaX, T, mx_size)
%MC Ellipsoid releasing;
% T is the query;
% DeltaX is the possible set;
% z is the released query answer;

% true_data === true_loc in genPossibleSet


%display(delta);
tic;
count=0;
euc_dist=0;
%check if true_data is in the possible set 'DeltaX'
possible_set=sum(DeltaX,1);
if(possible_set(state_no)==0)% true_data is not included;
    minDist=100000000000;
    for tmp=1:size(DeltaX,1)
        if(norm(T*true_loc'-T*(DeltaX(tmp,:))')<minDist)
            z_true=T*DeltaX(tmp,:)';
        end
    end
else% true_data is included;
    z_true=T*true_loc';
end
%z_true=T*true_data';





%for i=1:mx_size
%    if mod(floor(i/50),2)==1
%        T(1,i)=T(1,i)+0.05;
%    end
%end






%d=size(T,1);% answer dimensions;
d=1;
[A vertices ]=Find_Transf(DeltaX,T);% find  MEE; (P_i - c)' * A * (P_i - c) <= 1
if (sum(A==NaN)>0 )
    display(A);
end


if sum(size(A))==2 %only 1 state; 
    z=z_true;
    var_z=0;
    time_elps=toc;
    return;
end

%%%compute drift ratio
possible_set=sum(DeltaX,1);
count=0;
%drift_ratio=0;
%size_deltax=size(DeltaX,1);
if(possible_set(state_no)==0)% true_data is not included;
   count+=1;

end

[n1 var_z r sample]=IMNoise(A,d,eps,vertices);
z=z_true+n1;
time_elps=toc;

%%%compute dist
dist=zeros(2,1);
dist=(z-(T*true_loc')).^2
euc_dist=sqrt(sum(dist(:)));

end
%------------------------------------over--------------

function [Trans vertices]=Find_Transf(DeltaX,T)  %vertices:sensitivity hull
% find the minimum enclosing ellipsoid;

    Delta=T*DeltaX';% each column is a possible answer; 
    sz=size(Delta,2);
    P=[];
    sample_size=5000;   % l

    if sz==1 % there is only 1 possible state;
        Trans=1;
        vertices=Delta;
        return; 
    end

    for i=1:sz-1
        for j=i+1:sz
            P=[P,Delta(:,i)-Delta(:,j),Delta(:,j)-Delta(:,i)];
        end
    end
    if(sum(size(P))==0)
        display(DeltaX);
    end

    %check if points in P is lenear;
    PT=P';
    mb = [PT(:,1), ones(size(P,2),1)]\PT(:,2);

    %Function handle to see if each residual from the points to best fit line is less than some tolerance:
    islinear = @(mb,A,tol)all(abs((A(:,1).*mb(1)+mb(2)) - A(:,2))<tol);

    if size(P,2)<=2  || islinear(mb,PT,.05) %in case points are on a line;
         %mvn1=normrnd(0,0.1,2,1);mvn2=normrnd(0,0.1,2,1);
        % mvn1=mvn1/norm(mvn1)*0.1;
        % mvn2=mvn2/norm(mvn2)*0.1;
      % P=[P,(P(:,1)+P(:,2))/2+mvn1,-(P(:,1)+P(:,2))/2+mvn2];
        tmp1=P(:,1)-P(:,2);
        tmp=[tmp1(2,:); (-1)*tmp1(1,:)];
        P=[P,tmp/200,-tmp/200];
    end

    [vertice]=test_draw_polygon(P(1,:),P(2,:),size(P,2));
    N_vertex=size(vertice,2);

   max_sample_size=10000;
   pre_Trans=0;
   count1=0;
   for i=sample_size:1000:max_sample_size  %每次加1000
     count1=i;
     samples=test_sample(vertice,sample_size);  %从敏感度壳K中均匀的采样
     sample_center=mean(samples')';
     Y=zeros(2,2);
       for i=1:sample_size
        Y=Y+(samples(:,i)-sample_center)*(samples(:,i)-sample_center)';
       end
     Y=Y/sample_size;
     Trans=Y^(-1/2); %Trans就是文中的T    公式6
     %if T is stable
     if(norm((Trans-pre_Trans), 'fro' )<10e-4)
         break;
     end
     pre_Trans=Trans;

   end
    vertices=vertice; %敏感度壳K
end





function [n1 var1 r sample]=IMNoise(Trans,d,epsi,vertice)
    
    Iso_vertices=Trans*vertice;
    sample=test_sample(Iso_vertices,1);
    
    r=gamrnd(d,1/epsi);
    if(sum(sum(Trans==NaN))>0)
        display(Trans);
    end
    
    
    n1=r*Trans^(-1)*sample;
    var1=0;
end


function [vertice]=test_draw_polygon(X_x,X_y,n)
%draw the polygon

%figure;
%plot(X_x,X_y,'o');
[v]=convhull(X_x,X_y);
vertice=[X_x(v);X_y(v)];
end





function [samples]=test_sample(vertice,sample_size)
    total_length=0;
    N_vertex=size(vertice,2);
    segments=[];
    samples=[];
    vertice=[vertice,vertice(:,1)];
    for i=1:N_vertex
        segments=[segments,sqrt((vertice(1,i)-vertice(1,i+1))^2+(vertice(2,i)-vertice(2,i+1))^2)];
        total_length=total_length+sqrt((vertice(1,i)-vertice(1,i+1))^2+(vertice(2,i)-vertice(2,i+1))^2);
    end
    
    for i=1:sample_size
        r=rand(1,1)*total_length;
        for j=1:N_vertex
            if r>segments(j)
                r=r-segments(j);
            else
                break;
            end
        end
        
        samples=[samples,vertice(:,j)+(vertice(:,j+1)-vertice(:,j))*(r/segments(j))];
    end
end



%function [dist]=dist2line(x,y,x1,y1,x2,y2)
% calculate the distance between (x,y) and the line determined by (x1,y1)
% and (x2,y2);
    %dist=x*(y2-y1)-y*(x2-x1)-x1*y2+x2*y1;
%end
